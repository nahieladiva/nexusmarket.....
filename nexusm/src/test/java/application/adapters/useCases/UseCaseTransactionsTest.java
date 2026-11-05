package application.adapters.useCases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

/**
 * Every use case runs in a transaction, and queries run in a read-only one. {@code findCart} is not
 * a read-only operation because it creates the cart the first time.
 */
class UseCaseTransactionsTest {

    private static final List<Class<?>> USE_CASES = List.of(PublicAccessUseCase.class, AccountUseCase.class,
        BuyerUseCase.class, SellerUseCase.class, LogisticsOperatorUseCase.class,
        AdministratorUseCase.class, SupervisorUseCase.class);
    private static final Set<String> WRITING_QUERIES = Set.of("findCart");

    private boolean isQuery(Method method) {
        String name = method.getName();
        return (name.startsWith("find") || name.startsWith("search") || name.equals("validateToken"))
            && !WRITING_QUERIES.contains(name);
    }

    private boolean isReadOnly(Method method) {
        Transactional own = method.getAnnotation(Transactional.class);
        return own != null ? own.readOnly()
            : method.getDeclaringClass().getAnnotation(Transactional.class).readOnly();
    }

    @Test
    void everyUseCaseIsTransactional() {
        USE_CASES.forEach(useCase -> assertNotNull(useCase.getAnnotation(Transactional.class),
            useCase.getSimpleName()));
    }

    @Test
    void onlyQueriesAreReadOnly() {
        List<String> wrong = new ArrayList<>();
        for (Class<?> useCase : USE_CASES) {
            for (Method method : useCase.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && isQuery(method) != isReadOnly(method)) {
                    wrong.add(useCase.getSimpleName() + "." + method.getName());
                }
            }
        }
        assertEquals(List.of(), wrong);
    }
}
