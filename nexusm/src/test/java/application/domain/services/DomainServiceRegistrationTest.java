package application.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Spring only registers the classes marked with {@link DomainService}, and tests create the
 * services by hand, so a missing annotation would go unnoticed until the application starts.
 */
class DomainServiceRegistrationTest {

    private static final Path SERVICES = Path.of("src/main/java/application/domain/services");

    @Test
    void everyServiceInASubpackageIsMarked() throws IOException {
        try (Stream<Path> files = Files.walk(SERVICES)) {
            List<Path> services = files
                .filter(file -> file.toString().endsWith(".java"))
                .filter(file -> !file.getParent().equals(SERVICES))
                .toList();
            assertFalse(services.isEmpty());
            assertEquals(List.of(), services.stream().filter(this::isNotMarked).toList());
        }
    }

    private boolean isNotMarked(Path file) {
        try {
            return Files.readAllLines(file).stream().noneMatch(line -> line.equals("@DomainService"));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + file, e);
        }
    }
}
