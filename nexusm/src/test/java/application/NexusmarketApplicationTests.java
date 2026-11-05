package application;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Starts the full Spring context. It needs MySQL and MongoDB running
 * ({@code docker compose up -d mysql mongo}), so it is disabled by default and the
 * business rules are tested in {@code application.domain}.
 * To run it, remove {@code @Disabled} with the databases up.
 */
@Disabled("Requires MySQL and MongoDB started with docker compose")
@SpringBootTest
class NexusmarketApplicationTests {

    @Test
    void contextLoads() {
    }
}
