import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Read-only check that the SDD matches the compiled code. See tools/README.md.
 *
 * <p>Arguments: the repository directory and a file with the Maven dependency classpath. It exits
 * with 1 when there are failures. Warnings and unclassified citations do not fail the run.</p>
 */
public class VerifySdd {

    /** Names cited in the SDD that do not belong to the project. */
    private static final Set<String> EXTERNAL_NAMES = Set.of(
        "String", "List", "Set", "Map", "Optional", "LocalDateTime", "LocalDate", "Instant",
        "BigDecimal", "Long", "Integer", "Boolean", "UUID", "Object", "Collection", "Transactional",
        "Service", "Spring", "Lombok", "Jakarta", "Docker", "MySQL", "MongoDB", "JWT", "Duration",
        "Runnable", "Comparable", "IllegalArgumentException", "IllegalStateException");

    /** Field labels of each service, as written in the Spanish SDD. */
    private static final List<String> DETAIL_LABELS = List.of(
        "Propósito", "Quién lo usa", "Entradas", "Reglas de negocio", "Excepciones",
        "Estados que cambia");

    /** Role of each input port, as written in the Spanish SDD. */
    private static final Map<String, String> ROLE_OF_PORT = Map.of(
        "PublicAccessPort", "Anónimo",
        "AccountPort", "Cualquier usuario autenticado",
        "BuyerPort", "`BUYER`",
        "SellerPort", "`SELLER`",
        "LogisticsOperatorPort", "`LOGISTICS_OPERATOR`",
        "AdministratorPort", "`ADMINISTRATOR`",
        "SupervisorPort", "`SUPERVISOR`");

    private static final String ACTOR_SERVICE = "ResolveActorService";
    private static final String ANNOTATION_NAME = "DomainService";

    private static final Pattern CLASS_HEADER = Pattern.compile(
        "^(?:[a-z]+ )*(?:class|interface|enum) ([\\w.$]+)(?:<.*?>)?(?: extends ([\\w.$]+))?.*\\{$");
    private static final Pattern METHOD_HEADER = Pattern.compile(
        "^  (?!  )(?:.* )?([\\w.$]+)\\(.*\\)(?: throws .*)?;$");
    private static final Pattern INVOCATION = Pattern.compile(
        "// (?:Interface)?Method (?:([\\w/$]+)\\.)?\"?([<>\\w$]+)\"?:(\\S+)");
    private static final Pattern NEW_INSTANCE = Pattern.compile("\\bnew\\s+#\\d+\\s+// class ([\\w/$]+)");
    private static final Pattern SIGNATURE = Pattern.compile(
        "^([A-Z][\\w<>,\\[\\] .?]*|void|boolean|int|long) ([a-z]\\w*)\\((.*)\\)$");
    private static final Pattern QUALIFIED_MEMBER = Pattern.compile(
        "^([A-Z]\\w*)\\.([a-zA-Z_]\\w*)(\\((.*)\\))?$");
    private static final Pattern BARE_METHOD = Pattern.compile("^([a-zA-Z]\\w*)\\((.*)\\)$");
    private static final Pattern BACKTICK = Pattern.compile("`([^`]+)`");

    private static final Map<String, List<Class<?>>> classesBySimpleName = new TreeMap<>();
    private static final Map<String, Integer> passedChecks = new LinkedHashMap<>();
    private static final List<String> failures = new ArrayList<>();
    private static final List<String> warnings = new ArrayList<>();
    private static final Set<String> unclassified = new TreeSet<>();
    private static final Map<String, MethodNode> callGraph = new HashMap<>();
    private static final Map<String, String> superclassOf = new HashMap<>();

    private static Path repository;
    private static Path classesDirectory;
    private static ClassLoader classLoader;
    private static List<String> classNames;

    /** A method in the bytecode, with what it calls and what it instantiates. */
    private static final class MethodNode {
        private String owner;
        private String name;
        private String descriptor;
        private boolean usesLambda;
        private final Set<String> calls = new LinkedHashSet<>();
        private final Set<String> instantiated = new LinkedHashSet<>();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java tools/VerifySdd.java <repositoryDir> <classpathFile>");
            System.exit(2);
        }
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        repository = Path.of(args[0]).toAbsolutePath().normalize();
        classesDirectory = repository.resolve("nexusm/target/classes");
        loadClasses(Path.of(args[1]));
        readBytecode(classNames.stream()
            .filter(name -> name.startsWith("application.domain")
                || name.startsWith("application.adapters.useCases"))
            .toList());

        Path sdd = repository.resolve("SDD");
        List<Path> documents;
        try (Stream<Path> files = Files.walk(sdd)) {
            documents = files.filter(file -> file.toString().endsWith(".md")).sorted().toList();
        }
        for (Path document : documents) {
            checkCitations(sdd, document);
        }

        Map<String, Class<?>> services = findServices();
        Map<String, Set<String>> callsByOperation = new TreeMap<>();
        Map<String, Set<String>> operationsByService = new TreeMap<>();
        List<Class<?>> inputPorts = deriveUseCaseCalls(callsByOperation, operationsByService);
        Map<String, Set<String>> usersByService = deriveServiceUsers(services);

        checkInputPorts(sdd, inputPorts, callsByOperation);
        Set<String> auxiliary = checkMatrix(sdd, services, operationsByService, usersByService);
        checkServiceDetails(documents, services, operationsByService, usersByService, auxiliary);
        checkOutputPorts(sdd);
        checkModelApiIsCited(sdd);
        checkDomainImports();
        printReport(services.size());
        System.exit(failures.isEmpty() ? 0 : 1);
    }

    // ---- Loading

    private static void loadClasses(Path classpathFile) throws Exception {
        List<URL> urls = new ArrayList<>(List.of(classesDirectory.toUri().toURL()));
        for (String entry : Files.readString(classpathFile).trim().split(java.io.File.pathSeparator)) {
            urls.add(Path.of(entry).toUri().toURL());
        }
        classLoader = new URLClassLoader(urls.toArray(URL[]::new));
        try (Stream<Path> files = Files.walk(classesDirectory)) {
            classNames = files.filter(file -> file.toString().endsWith(".class"))
                .map(file -> classesDirectory.relativize(file).toString()
                    .replace('\\', '.').replace('/', '.').replaceAll("\\.class$", ""))
                .sorted()
                .toList();
        }
        for (String name : classNames) {
            Class<?> loaded = load(name);
            if (!loaded.isAnonymousClass() && !loaded.isSynthetic()) {
                classesBySimpleName.computeIfAbsent(loaded.getSimpleName(), key -> new ArrayList<>())
                    .add(loaded);
            }
        }
    }

    private static Class<?> load(String name) throws ClassNotFoundException {
        return Class.forName(name, false, classLoader);
    }

    private static void readBytecode(List<String> names) {
        ToolProvider javap = ToolProvider.findFirst("javap").orElseThrow();
        int batchSize = 60;
        for (int start = 0; start < names.size(); start += batchSize) {
            List<String> arguments = new ArrayList<>(
                List.of("-c", "-p", "-s", "-cp", classesDirectory.toString()));
            arguments.addAll(names.subList(start, Math.min(start + batchSize, names.size())));
            StringWriter output = new StringWriter();
            PrintWriter writer = new PrintWriter(output);
            javap.run(writer, writer, arguments.toArray(String[]::new));
            parseJavapOutput(output.toString());
        }
        linkLambdas();
    }

    private static void parseJavapOutput(String output) {
        String currentClass = null;
        String pendingMethod = null;
        MethodNode method = null;
        for (String line : output.split("\\R")) {
            Matcher classHeader = CLASS_HEADER.matcher(line);
            if (classHeader.find() && !line.startsWith(" ")) {
                currentClass = classHeader.group(1).replace('.', '/');
                if (classHeader.group(2) != null) {
                    superclassOf.put(currentClass, classHeader.group(2).replace('.', '/'));
                }
                method = null;
                continue;
            }
            if (line.equals("  static {};")) {
                pendingMethod = "<clinit>";
                continue;
            }
            Matcher methodHeader = METHOD_HEADER.matcher(line);
            if (methodHeader.find()) {
                String name = methodHeader.group(1);
                pendingMethod = name.replace('.', '/').equals(currentClass) ? "<init>" : name;
                continue;
            }
            String text = line.trim();
            if (text.startsWith("descriptor: ") && pendingMethod != null) {
                method = new MethodNode();
                method.owner = currentClass;
                method.name = pendingMethod;
                method.descriptor = text.substring("descriptor: ".length());
                callGraph.put(key(currentClass, pendingMethod, method.descriptor), method);
                pendingMethod = null;
                continue;
            }
            if (method != null) {
                readInstruction(text, currentClass, method);
            }
        }
    }

    private static void readInstruction(String text, String currentClass, MethodNode method) {
        if (text.contains("invokedynamic")) {
            method.usesLambda = true;
        }
        Matcher instantiation = NEW_INSTANCE.matcher(text);
        if (instantiation.find()) {
            method.instantiated.add(instantiation.group(1));
        }
        if (text.matches("\\d+: invoke.*")) {
            Matcher invocation = INVOCATION.matcher(text);
            if (invocation.find()) {
                String owner = invocation.group(1) == null ? currentClass : invocation.group(1);
                method.calls.add(key(owner, invocation.group(2), invocation.group(3)));
            }
        }
    }

    /**
     * Lambdas are named after their enclosing method. Method references carry no such name, so in
     * services a method with a lambda is linked to its sibling methods. That is an over-approximation.
     */
    private static void linkLambdas() {
        List<MethodNode> nodes = new ArrayList<>(callGraph.values());
        for (MethodNode method : nodes) {
            if (!method.usesLambda) {
                continue;
            }
            String enclosing = method.name.equals("<init>") ? "new" : method.name;
            for (MethodNode other : nodes) {
                if (!other.owner.equals(method.owner)) {
                    continue;
                }
                boolean ownLambda = other.name.startsWith("lambda$" + enclosing + "$");
                boolean sibling = !other.name.startsWith("lambda$") && !other.name.equals("<init>")
                    && !other.name.equals(method.name) && method.owner.contains("/services/");
                if (ownLambda || sibling) {
                    method.calls.add(key(other.owner, other.name, other.descriptor));
                }
            }
        }
    }

    private static String key(String owner, String name, String descriptor) {
        return owner + "#" + name + "#" + descriptor;
    }

    private static String descriptorOf(Method method) {
        return MethodType.methodType(method.getReturnType(), method.getParameterTypes())
            .toMethodDescriptorString();
    }

    private static String resolveInHierarchy(String owner, String name, String descriptor) {
        for (String current = owner; current != null; current = superclassOf.get(current)) {
            String candidate = key(current, name, descriptor);
            if (callGraph.containsKey(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static Set<String> reachableExceptions(String start) {
        Set<String> visited = new HashSet<>();
        Set<String> exceptions = new TreeSet<>();
        Deque<String> queue = new ArrayDeque<>(List.of(start));
        while (!queue.isEmpty()) {
            String[] parts = queue.poll().split("#", 3);
            String resolved = resolveInHierarchy(parts[0], parts[1], parts[2]);
            if (resolved == null || !visited.add(resolved)) {
                continue;
            }
            MethodNode method = callGraph.get(resolved);
            for (String instantiated : method.instantiated) {
                if (instantiated.startsWith("application/domain/exceptions/")
                        || instantiated.equals("java/lang/IllegalArgumentException")
                        || instantiated.equals("java/lang/IllegalStateException")) {
                    exceptions.add(simpleName(instantiated));
                }
            }
            queue.addAll(method.calls);
        }
        return exceptions;
    }

    // ---- Citations

    private static void checkCitations(Path sdd, Path document) throws Exception {
        String name = sdd.relativize(document).toString().replace('\\', '/');
        boolean usesContext = name.endsWith("Domain Model.md") || name.contains("Value Objects")
            || name.startsWith("domain/services/") || name.endsWith("Input-ports.md");
        Class<?> context = null;
        Class<?> diagramClass = null;
        boolean inFence = false;
        boolean inMermaid = false;
        int lineNumber = 0;
        for (String line : Files.readAllLines(document, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.startsWith("```")) {
                inFence = !inFence;
                inMermaid = inFence && line.contains("mermaid");
                diagramClass = null;
                continue;
            }
            if (inFence) {
                if (inMermaid) {
                    diagramClass = checkDiagramLine(name, lineNumber, line, diagramClass);
                }
                continue;
            }
            if (line.startsWith("#")) {
                context = usesContext ? contextOf(line) : null;
            }
            for (String citation : backticked(line)) {
                checkCitation(name, lineNumber, citation, context);
            }
        }
    }

    private static Class<?> checkDiagramLine(String file, int lineNumber, String line, Class<?> current) {
        Class<?> diagramClass = current;
        Matcher declaration = Pattern.compile("^\\s*class (\\w+)").matcher(line);
        if (declaration.find()) {
            diagramClass = firstClass(declaration.group(1));
            if (diagramClass == null) {
                fail("diagrams", file + ":" + lineNumber + " class " + declaration.group(1));
            } else {
                pass("diagrams");
            }
        }
        Matcher member = Pattern.compile("^\\s*[+\\-#]?(?:[\\w<>,]+ )?(\\w+)\\(").matcher(line);
        if (diagramClass != null && member.find()) {
            if (hasMember(diagramClass, member.group(1))) {
                pass("diagrams");
            } else {
                fail("diagrams", file + ":" + lineNumber + " "
                    + diagramClass.getSimpleName() + "." + member.group(1));
            }
        }
        return line.contains("}") ? null : diagramClass;
    }

    private static Class<?> contextOf(String heading) {
        Matcher matcher = Pattern.compile("^#{2,4} .*?`([A-Z]\\w*)`").matcher(heading);
        return matcher.find() ? firstClass(matcher.group(1)) : null;
    }

    private static void checkCitation(String file, int line, String citation, Class<?> context) {
        String where = file + ":" + line + " `" + citation + "`";
        Matcher matcher = SIGNATURE.matcher(citation);
        if (matcher.matches()) {
            checkSignature(where, matcher, context);
            return;
        }
        matcher = QUALIFIED_MEMBER.matcher(citation);
        if (matcher.matches()) {
            checkQualifiedMember(where, matcher);
            return;
        }
        matcher = BARE_METHOD.matcher(citation);
        if (matcher.matches()) {
            checkBareMethod(where, matcher.group(1), matcher.group(2), context);
            return;
        }
        if (citation.matches("^(application|adapters|domain|infrastructure)\\..*")) {
            checkPackage(where, citation);
            return;
        }
        if (citation.matches("^[A-Z]\\w*[a-z]\\w*(<.*>)?(\\[\\])?$") || citation.matches("^[A-Z]\\w*<.*>$")) {
            checkTypeNames(where, citation);
            return;
        }
        if (citation.matches("^[A-Z][A-Z0-9_]+$") && (context != null && hasMember(context, citation)
                || isDomainMember(citation))) {
            pass("constants");
            return;
        }
        if (citation.matches("^[a-z]\\w*$") && context != null) {
            if (hasMember(context, citation)) {
                pass("fields in context");
            } else {
                warnings.add("not a member of " + context.getSimpleName() + ": " + where);
            }
            return;
        }
        if (citation.matches("^[a-z]\\w*$") && isDomainMember(citation)) {
            pass("members without context");
            return;
        }
        unclassified.add(citation);
    }

    private static void checkSignature(String where, Matcher matcher, Class<?> context) {
        String name = matcher.group(2);
        List<String> arguments = splitArguments(matcher.group(3));
        Collection<Class<?>> candidates = context != null ? List.of(context) : allClasses();
        boolean found = candidates.stream().flatMap(type -> methodsNamed(type, name).stream())
            .anyMatch(method -> parametersMatch(method, arguments));
        if (found) {
            pass("cited signatures");
        } else {
            fail("cited signatures", where);
        }
    }

    private static void checkQualifiedMember(String where, Matcher matcher) {
        Class<?> type = firstClass(matcher.group(1));
        if (type == null) {
            fail("Class.member", where + " class does not exist");
            return;
        }
        String member = matcher.group(2);
        if (!hasMember(type, member)) {
            fail("Class.member", where + " member does not exist");
            return;
        }
        String arguments = matcher.group(4);
        if (arguments != null && !arguments.isBlank() && !arguments.equals("...")) {
            List<String> expected = splitArguments(arguments);
            if (methodsNamed(type, member).stream().noneMatch(m -> parametersMatch(m, expected))) {
                fail("Class.member", where + " parameters do not match");
                return;
            }
        }
        pass("Class.member");
    }

    private static void checkBareMethod(String where, String name, String arguments, Class<?> context) {
        if (context == null) {
            if (isDomainMember(name)) {
                pass("methods without context");
            } else {
                fail("methods without context", where);
            }
            return;
        }
        List<Method> methods = methodsNamed(context, name);
        boolean constructor = name.equals(context.getSimpleName());
        if (methods.isEmpty() && !constructor && !(context.isEnum() && hasMember(context, name))) {
            fail("methods in context", where + " is not in " + context.getSimpleName());
            return;
        }
        if (!constructor && !arguments.equals("...") && !methods.isEmpty()) {
            List<String> expected = splitArguments(arguments);
            if (methods.stream().noneMatch(method -> parametersMatch(method, expected))) {
                fail("methods in context", where + " actual: "
                    + methods.stream().map(VerifySdd::signatureOf).toList());
                return;
            }
        }
        pass("methods in context");
    }

    private static void checkPackage(String where, String citation) {
        String relative = citation.replaceFirst("^application\\.", "").replace('.', '/')
            .replaceAll("/\\*$", "");
        Path sources = repository.resolve("nexusm/src/main/java/application");
        if (Files.exists(sources.resolve(relative)) || Files.exists(sources.resolve(relative + ".java"))) {
            pass("packages");
        } else {
            fail("packages", where);
        }
    }

    private static void checkTypeNames(String where, String citation) {
        boolean allExist = true;
        Matcher identifier = Pattern.compile("[A-Za-z]\\w*").matcher(citation);
        while (identifier.find()) {
            String name = identifier.group();
            if (!classesBySimpleName.containsKey(name) && !EXTERNAL_NAMES.contains(name)) {
                allExist = false;
                fail("classes", where + " does not exist: " + name);
            }
        }
        if (allExist) {
            pass("classes");
        }
    }

    // ---- Derived facts

    private static Map<String, Class<?>> findServices() throws Exception {
        Map<String, Class<?>> services = new TreeMap<>();
        for (String name : classNames) {
            if (name.startsWith("application.domain.services.") && !name.contains("$")) {
                Class<?> type = load(name);
                if (!type.isAnnotation()) {
                    services.put(type.getSimpleName(), type);
                }
            }
        }
        return services;
    }

    private static List<Class<?>> deriveUseCaseCalls(Map<String, Set<String>> callsByOperation,
                                                     Map<String, Set<String>> operationsByService)
            throws Exception {
        List<Class<?>> ports = new ArrayList<>();
        Map<String, Class<?>> useCaseByPort = new HashMap<>();
        for (String name : classNames) {
            if (name.startsWith("application.domain.ports.in.")) {
                ports.add(load(name));
            }
            if (name.startsWith("application.adapters.useCases.") && !name.contains("$")) {
                Class<?> useCase = load(name);
                for (Class<?> port : useCase.getInterfaces()) {
                    useCaseByPort.put(port.getSimpleName(), useCase);
                }
            }
        }
        for (Class<?> port : ports) {
            Class<?> useCase = useCaseByPort.get(port.getSimpleName());
            if (useCase == null) {
                fail("input ports", port.getSimpleName() + " has no use case");
                continue;
            }
            for (Method method : port.getDeclaredMethods()) {
                Set<String> called = serviceCallsOf(useCase, method);
                Set<String> business = called.stream()
                    .filter(call -> !call.startsWith(ACTOR_SERVICE))
                    .collect(Collectors.toCollection(TreeSet::new));
                Set<String> effective = business.isEmpty() ? called : business;
                String operation = port.getSimpleName() + "." + method.getName();
                callsByOperation.put(operation, effective);
                for (String call : effective) {
                    operationsByService.computeIfAbsent(call.split("\\.")[0], k -> new TreeSet<>())
                        .add(operation);
                }
            }
        }
        return ports;
    }

    private static Set<String> serviceCallsOf(Class<?> useCase, Method method) {
        String owner = useCase.getName().replace('.', '/');
        Set<String> called = new TreeSet<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>(List.of(key(owner, method.getName(), descriptorOf(method))));
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            MethodNode node = callGraph.get(current);
            if (node == null) {
                fail("input ports", "no bytecode for " + current);
                continue;
            }
            for (String call : node.calls) {
                String[] parts = call.split("#", 3);
                if (parts[0].equals(owner)) {
                    queue.add(call);
                } else if (parts[0].startsWith("application/domain/services/")) {
                    called.add(simpleName(parts[0]) + "." + parts[1]);
                }
            }
        }
        return called;
    }

    private static Map<String, Set<String>> deriveServiceUsers(Map<String, Class<?>> services) {
        Map<String, Set<String>> users = new TreeMap<>();
        for (Class<?> service : services.values()) {
            for (Field field : service.getDeclaredFields()) {
                String dependency = field.getType().getSimpleName();
                if (services.containsKey(dependency)) {
                    users.computeIfAbsent(dependency, k -> new TreeSet<>()).add(service.getSimpleName());
                }
            }
        }
        return users;
    }

    // ---- Documents against derived facts

    private static void checkInputPorts(Path sdd, List<Class<?>> ports,
                                        Map<String, Set<String>> callsByOperation) throws Exception {
        List<String> lines = Files.readAllLines(sdd.resolve("domain/Input-ports.md"), StandardCharsets.UTF_8);
        Map<String, Set<String>> documented = new HashMap<>();
        String port = null;
        for (String line : lines) {
            Matcher heading = Pattern.compile("^## `(\\w+)`").matcher(line);
            if (heading.find()) {
                port = heading.group(1);
                documented.put(port, new HashSet<>());
                continue;
            }
            if (port == null || !line.startsWith("| `")) {
                continue;
            }
            String[] cells = line.split("\\|");
            String signature = backticked(cells[1]).get(0);
            documented.get(port).add(signature);
            String method = signature.replaceAll("\\(.*", "").replaceAll(".* ", "");
            Set<String> actual = callsByOperation.get(port + "." + method);
            Set<String> cited = new TreeSet<>(backticked(cells[2]));
            if (actual == null) {
                fail("port to service", port + "." + method + " does not exist");
            } else if (actual.equals(cited)) {
                pass("port to service");
            } else {
                fail("port to service", port + "." + method + " sdd=" + cited + " code=" + actual);
            }
        }
        int total = 0;
        for (Class<?> type : ports) {
            String name = type.getSimpleName();
            Set<String> actual = Arrays.stream(type.getDeclaredMethods()).map(VerifySdd::signatureOf)
                .collect(Collectors.toSet());
            Set<String> cited = documented.getOrDefault(name, Set.of());
            for (String signature : actual) {
                if (cited.contains(signature)) {
                    pass("input port signatures");
                } else {
                    fail("input port signatures", "missing in SDD: " + name + ": " + signature);
                }
            }
            minus(cited, actual).forEach(signature ->
                fail("input port signatures", "not in code: " + name + ": " + signature));
            total += actual.size();
            String row = lines.stream().filter(line -> line.startsWith("| `" + name + "` |"))
                .findFirst().orElse("");
            if (row.matches(".*\\| " + actual.size() + " \\|$")) {
                pass("operation counts");
            } else {
                fail("operation counts", name + " code=" + actual.size() + " row=" + row);
            }
        }
        String expectedTotal = "**" + total + "**";
        if (lines.stream().anyMatch(line -> line.startsWith("| **Total**") && line.contains(expectedTotal))) {
            pass("operation counts");
        } else {
            fail("operation counts", "total in code: " + total);
        }
    }

    private static Set<String> checkMatrix(Path sdd, Map<String, Class<?>> services,
                                           Map<String, Set<String>> operationsByService,
                                           Map<String, Set<String>> usersByService) throws Exception {
        Set<String> exposed = new TreeSet<>();
        Set<String> auxiliary = new TreeSet<>();
        int section = 0;
        for (String line : Files.readAllLines(sdd.resolve("domain/Services.md"), StandardCharsets.UTF_8)) {
            if (line.startsWith("## 1")) {
                section = 1;
            }
            if (line.startsWith("## 2")) {
                section = 2;
            }
            if (!line.startsWith("| `")) {
                continue;
            }
            String[] cells = line.split("\\|");
            String service = backticked(cells[1]).get(0);
            if (!services.containsKey(service)) {
                fail("matrix", service + " is not a service");
                continue;
            }
            String subpackage = services.get(service).getPackageName().replaceAll(".*\\.", "");
            if (cells[cells.length - 1].contains("services/" + subpackage + ".md")) {
                pass("matrix");
            } else {
                fail("matrix", service + " has a wrong link: " + cells[cells.length - 1]);
            }
            if (section == 1) {
                exposed.add(service);
                checkExposedRow(service, cells, operationsByService);
            } else {
                auxiliary.add(service);
                checkAuxiliaryRow(service, cells, operationsByService, usersByService);
            }
        }
        for (String service : services.keySet()) {
            if (!exposed.contains(service) && !auxiliary.contains(service)
                    && !service.equals(ANNOTATION_NAME)) {
                fail("matrix", "service without a row: " + service);
            }
        }
        for (String service : operationsByService.keySet()) {
            if (!exposed.contains(service)) {
                fail("matrix", "exposed but not in section 1: " + service);
            }
        }
        return auxiliary;
    }

    private static void checkExposedRow(String service, String[] cells,
                                        Map<String, Set<String>> operationsByService) {
        if (service.equals(ACTOR_SERVICE)) {
            pass("matrix");
            return;
        }
        Set<String> cited = new TreeSet<>(backticked(cells[3]));
        Set<String> actual = operationsByService.getOrDefault(service, Set.of());
        compare("matrix", service + " operations", cited, actual);
        Set<String> actualRoles = actual.stream().map(op -> ROLE_OF_PORT.get(op.split("\\.")[0]))
            .collect(Collectors.toCollection(TreeSet::new));
        Set<String> citedRoles = Arrays.stream(cells[2].split(",")).map(String::trim)
            .collect(Collectors.toCollection(TreeSet::new));
        compare("matrix", service + " roles", citedRoles, actualRoles);
    }

    private static void checkAuxiliaryRow(String service, String[] cells,
                                          Map<String, Set<String>> operationsByService,
                                          Map<String, Set<String>> usersByService) {
        if (operationsByService.containsKey(service)) {
            fail("matrix", service + " is auxiliary but a port calls it: "
                + operationsByService.get(service));
        }
        compare("matrix", service + " used by", new TreeSet<>(backticked(cells[2])),
            usersByService.getOrDefault(service, Set.of()));
    }

    private static void checkServiceDetails(List<Path> documents, Map<String, Class<?>> services,
                                            Map<String, Set<String>> operationsByService,
                                            Map<String, Set<String>> usersByService,
                                            Set<String> auxiliary) throws Exception {
        Set<String> detailed = new TreeSet<>();
        for (Path document : documents) {
            if (!document.getParent().getFileName().toString().equals("services")) {
                continue;
            }
            String subpackage = document.getFileName().toString().replace(".md", "");
            for (var entry : readServiceSections(document).entrySet()) {
                String name = entry.getKey();
                Class<?> service = services.get(name);
                if (service == null) {
                    fail("details", subpackage + ": " + name + " does not exist");
                    continue;
                }
                detailed.add(name);
                if (!service.getPackageName().endsWith("." + subpackage)) {
                    fail("details", name + " is not in package " + subpackage);
                }
                checkServiceSection(name, service, entry.getValue(), operationsByService,
                    usersByService, auxiliary);
            }
        }
        for (String service : services.keySet()) {
            if (!service.equals(ANNOTATION_NAME) && !detailed.contains(service)) {
                fail("details", "service without a section: " + service);
            }
        }
    }

    private static Map<String, Map<String, String>> readServiceSections(Path document) throws Exception {
        Map<String, Map<String, String>> sections = new LinkedHashMap<>();
        String service = null;
        for (String line : Files.readAllLines(document, StandardCharsets.UTF_8)) {
            Matcher heading = Pattern.compile("^### `(\\w+)`").matcher(line);
            if (heading.find()) {
                service = heading.group(1);
                sections.put(service, new HashMap<>());
                continue;
            }
            Matcher field = Pattern.compile("^- \\*\\*(.+?):\\*\\* (.*)$").matcher(line);
            if (field.find() && service != null) {
                sections.get(service).put(field.group(1), field.group(2));
            }
        }
        return sections;
    }

    private static void checkServiceSection(String name, Class<?> service, Map<String, String> fields,
                                            Map<String, Set<String>> operationsByService,
                                            Map<String, Set<String>> usersByService,
                                            Set<String> auxiliary) {
        for (String label : DETAIL_LABELS) {
            if (fields.containsKey(label)) {
                pass("six fields per service");
            } else {
                fail("six fields per service", name + " has no " + label);
            }
        }
        List<Method> publicMethods = Arrays.stream(service.getDeclaredMethods())
            .filter(method -> Modifier.isPublic(method.getModifiers()) && !method.isSynthetic())
            .toList();
        Set<String> signatures = publicMethods.stream().map(VerifySdd::signatureOf)
            .collect(Collectors.toCollection(TreeSet::new));
        compare("inputs are the public methods", name,
            new TreeSet<>(backticked(fields.getOrDefault("Entradas", ""))), signatures);

        if (!name.equals(ACTOR_SERVICE)) {
            List<String> users = backticked(fields.getOrDefault("Quién lo usa", ""));
            Set<String> citedOperations = users.stream().filter(user -> user.contains("."))
                .collect(Collectors.toCollection(TreeSet::new));
            compare("used by", name, citedOperations, operationsByService.getOrDefault(name, Set.of()));
            if (auxiliary.contains(name)) {
                Set<String> citedServices = users.stream().filter(user -> !user.contains("."))
                    .collect(Collectors.toCollection(TreeSet::new));
                compare("used by", name, citedServices, usersByService.getOrDefault(name, Set.of()));
            }
        }
        checkExceptions(name, service, publicMethods, fields.getOrDefault("Excepciones", ""));
    }

    private static void checkExceptions(String name, Class<?> service, List<Method> publicMethods,
                                        String text) {
        Set<String> cited = backticked(text).stream().filter(item -> item.endsWith("Exception"))
            .collect(Collectors.toCollection(TreeSet::new));
        Set<String> reachable = new TreeSet<>();
        String owner = service.getName().replace('.', '/');
        for (Method method : publicMethods) {
            reachable.addAll(reachableExceptions(key(owner, method.getName(), descriptorOf(method))));
        }
        Set<String> impossible = minus(cited, reachable);
        if (impossible.isEmpty()) {
            pass("cited exceptions are reachable");
        } else {
            fail("cited exceptions are reachable", name + " cites " + impossible
                + " but reaches " + reachable);
        }
        Set<String> notCited = minus(reachable, cited);
        notCited.remove("IllegalArgumentException");
        notCited.remove("IllegalStateException");
        if (!notCited.isEmpty()) {
            warnings.add("exception not cited in " + name + ": " + notCited);
        }
    }

    private static void checkOutputPorts(Path sdd) throws Exception {
        Map<String, String> rows = new HashMap<>();
        for (String line : Files.readAllLines(sdd.resolve("domain/Output-ports.md"), StandardCharsets.UTF_8)) {
            if (line.startsWith("| `")) {
                rows.put(backticked(line.split("\\|")[1]).get(0), line);
            }
        }
        for (String name : classNames) {
            if (!name.startsWith("application.domain.ports.out.") || name.contains("$")) {
                continue;
            }
            Class<?> port = load(name);
            String row = rows.remove(port.getSimpleName());
            if (row == null) {
                fail("output ports", "missing in SDD: " + port.getSimpleName());
                continue;
            }
            String[] cells = row.split("\\|");
            Set<String> methods = Arrays.stream(port.getDeclaredMethods())
                .filter(method -> !method.isSynthetic()).map(Method::getName)
                .collect(Collectors.toCollection(TreeSet::new));
            compare("output ports", port.getSimpleName() + " methods",
                new TreeSet<>(backticked(cells[3])), methods);
            compare("output ports", port.getSimpleName() + " adapters",
                new TreeSet<>(backticked(cells[2])), implementationsOf(port));
        }
        rows.keySet().forEach(name -> fail("output ports", "not in code: " + name));
    }

    private static Set<String> implementationsOf(Class<?> port) throws Exception {
        Set<String> implementations = new TreeSet<>();
        for (String name : classNames) {
            if (name.startsWith("application.adapters.") || name.startsWith("application.infrastructure")) {
                Class<?> type = load(name);
                if (!type.isInterface() && port.isAssignableFrom(type)) {
                    implementations.add(type.getSimpleName());
                }
            }
        }
        return implementations;
    }

    private static void checkModelApiIsCited(Path sdd) throws Exception {
        String model = Files.readString(sdd.resolve("domain/Domain Model.md"))
            + Files.readString(sdd.resolve("domain/Domain Value Objects.md"));
        for (String name : classNames) {
            boolean inModel = name.startsWith("application.domain.models.")
                || name.startsWith("application.domain.valueobjects.")
                || name.startsWith("application.domain.enums.");
            if (!inModel || name.contains("$")) {
                continue;
            }
            Class<?> type = load(name);
            Set<String> notCited = new TreeSet<>();
            for (Method method : type.getDeclaredMethods()) {
                String methodName = method.getName();
                boolean ignored = !Modifier.isPublic(method.getModifiers()) || method.isSynthetic()
                    || methodName.startsWith("get")
                    || methodName.matches("equals|hashCode|toString|values|valueOf|value")
                    || isRecordAccessor(type, method);
                if (!ignored && !model.contains(methodName)) {
                    notCited.add(methodName);
                }
            }
            if (!notCited.isEmpty()) {
                warnings.add("public API not cited in the model: " + type.getSimpleName() + " " + notCited);
            }
        }
    }

    private static boolean isRecordAccessor(Class<?> type, Method method) {
        return type.isRecord() && method.getParameterCount() == 0
            && Arrays.stream(type.getRecordComponents())
                .anyMatch(component -> component.getName().equals(method.getName()));
    }

    private static void checkDomainImports() throws Exception {
        String forbidden = "import (static )?(org\\.springframework|lombok|jakarta"
            + "|application\\.adapters|application\\.infrastructure).*";
        List<Path> sources;
        try (Stream<Path> files = Files.walk(repository.resolve("nexusm/src/main/java/application/domain"))) {
            sources = files.filter(file -> file.toString().endsWith(".java")).toList();
        }
        for (Path source : sources) {
            boolean clean = true;
            for (String line : Files.readAllLines(source, StandardCharsets.UTF_8)) {
                if (line.matches(forbidden)) {
                    clean = false;
                    fail("pure domain", source.getFileName() + ": " + line);
                }
            }
            if (clean) {
                pass("pure domain");
            }
        }
    }

    // ---- Reflection helpers

    private static Collection<Class<?>> allClasses() {
        return classesBySimpleName.values().stream().flatMap(List::stream).toList();
    }

    private static Class<?> firstClass(String simpleName) {
        List<Class<?>> classes = classesBySimpleName.get(simpleName);
        return classes == null ? null : classes.get(0);
    }

    private static List<Class<?>> hierarchyOf(Class<?> type) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Deque<Class<?>> queue = new ArrayDeque<>(List.of(type));
        while (!queue.isEmpty()) {
            Class<?> current = queue.poll();
            if (current == Object.class || hierarchy.contains(current)) {
                continue;
            }
            hierarchy.add(current);
            if (current.getSuperclass() != null) {
                queue.add(current.getSuperclass());
            }
            queue.addAll(List.of(current.getInterfaces()));
        }
        return hierarchy;
    }

    private static boolean hasMember(Class<?> type, String name) {
        if (type.isEnum() && Set.of("values", "valueOf", "name", "ordinal").contains(name)) {
            return true;
        }
        for (Class<?> current : hierarchyOf(type)) {
            boolean found = Arrays.stream(current.getDeclaredMethods()).anyMatch(m -> m.getName().equals(name))
                || Arrays.stream(current.getDeclaredFields()).anyMatch(f -> f.getName().equals(name))
                || Arrays.stream(current.getDeclaredClasses()).anyMatch(c -> c.getSimpleName().equals(name));
            if (found) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDomainMember(String name) {
        return allClasses().stream().filter(type -> type.getName().startsWith("application.domain"))
            .anyMatch(type -> hasMember(type, name));
    }

    private static List<Method> methodsNamed(Class<?> type, String name) {
        List<Method> methods = new ArrayList<>();
        for (Class<?> current : hierarchyOf(type)) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && !method.isSynthetic()) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    private static String typeName(Type type) {
        return type.getTypeName().replaceAll("(\\w+[.$])+", "");
    }

    private static String parameterType(Method method, int index) {
        Type[] types = method.getGenericParameterTypes();
        String name = typeName(types[index]);
        return method.isVarArgs() && index == types.length - 1 ? name.replaceAll("\\[\\]$", "...") : name;
    }

    private static String signatureOf(Method method) {
        Parameter[] parameters = method.getParameters();
        List<String> rendered = new ArrayList<>();
        for (int index = 0; index < parameters.length; index++) {
            String name = parameters[index].isNamePresent() ? parameters[index].getName() : "?";
            rendered.add(parameterType(method, index) + " " + name);
        }
        return typeName(method.getGenericReturnType()) + " " + method.getName()
            + "(" + String.join(", ", rendered) + ")";
    }

    private static boolean parametersMatch(Method method, List<String> arguments) {
        if (method.getParameterCount() != arguments.size()) {
            return false;
        }
        for (int index = 0; index < arguments.size(); index++) {
            String type = parameterType(method, index);
            String raw = type.replaceAll("<.*>", "");
            String argument = arguments.get(index);
            if (!argument.equals(type) && !argument.equals(raw)
                    && !argument.startsWith(type + " ") && !argument.startsWith(raw + " ")) {
                return false;
            }
        }
        return true;
    }

    // ---- Text helpers

    private static List<String> backticked(String text) {
        List<String> values = new ArrayList<>();
        Matcher matcher = BACKTICK.matcher(text);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return values;
    }

    private static List<String> splitArguments(String text) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (char character : text.toCharArray()) {
            depth += character == '<' ? 1 : character == '>' ? -1 : 0;
            if (character == ',' && depth == 0) {
                arguments.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (!current.toString().isBlank()) {
            arguments.add(current.toString().trim());
        }
        return arguments;
    }

    private static String simpleName(String internalName) {
        return internalName.substring(internalName.lastIndexOf('/') + 1);
    }

    private static <T> Set<T> minus(Set<T> first, Set<T> second) {
        Set<T> result = new TreeSet<>(first);
        result.removeAll(second);
        return result;
    }

    // ---- Results

    private static void compare(String check, String subject, Set<String> cited, Set<String> actual) {
        if (cited.equals(actual)) {
            pass(check);
        } else {
            fail(check, subject + ": not in code=" + minus(cited, actual)
                + " missing in SDD=" + minus(actual, cited));
        }
    }

    private static void pass(String check) {
        passedChecks.merge(check, 1, Integer::sum);
    }

    private static void fail(String check, String message) {
        passedChecks.merge(check, 0, Integer::sum);
        failures.add("[" + check + "] " + message);
    }

    private static void printReport(int serviceCount) {
        System.out.println("Classes: " + classNames.size() + ", methods in the graph: " + callGraph.size()
            + ", services: " + serviceCount);
        Map<String, Long> failuresByCheck = failures.stream().collect(Collectors.groupingBy(
            failure -> failure.substring(1, failure.indexOf(']')), Collectors.counting()));
        System.out.println("\n=== SUMMARY ===");
        passedChecks.forEach((check, passed) -> System.out.printf("%-34s passed=%-5d failed=%d%n",
            check, passed, failuresByCheck.getOrDefault(check, 0L)));
        int passedTotal = passedChecks.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("TOTAL passed=" + passedTotal + " failed=" + failures.size());
        System.out.println("\n=== FAILURES (" + failures.size() + ") ===");
        failures.forEach(System.out::println);
        System.out.println("\n=== WARNINGS (" + warnings.size() + ") ===");
        warnings.forEach(System.out::println);
        System.out.println("\n=== UNCLASSIFIED (" + unclassified.size() + ") ===");
        System.out.println(unclassified);
    }
}
