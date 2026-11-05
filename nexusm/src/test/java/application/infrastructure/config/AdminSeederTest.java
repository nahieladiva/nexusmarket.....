package application.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import application.domain.enums.UserRole;
import application.domain.models.Administrator;
import application.domain.models.AuthSession;
import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

/**
 * Tests of the initial administrator.
 */
class AdminSeederTest {

    private static final Email EMAIL = Email.of("admin@nexusmarket.com");
    private static final String PASSWORD = "Admin2026";

    private TestFixture f;
    private AdminSeeder seeder;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        seeder = new AdminSeeder(f.users, f.passwordEncoder, f.ids, f.clock, EMAIL.getValue(), PASSWORD);
    }

    @Test
    void createsAnAdministratorWithItsRole() {
        run();

        User stored = f.users.findByEmail(EMAIL).orElseThrow();
        assertInstanceOf(Administrator.class, stored);
        assertEquals(UserRole.ADMINISTRATOR, stored.getRole());
    }

    @Test
    void theAdministratorCanLogInAndTheTokenCarriesItsRole() {
        run();

        AuthSession session = f.login.execute(EMAIL, PASSWORD);

        assertEquals(UserRole.ADMINISTRATOR, f.validateToken.execute(session.token()).role());
    }

    @Test
    void runningTwiceKeepsASingleAdministrator() {
        run();
        User first = f.users.findByEmail(EMAIL).orElseThrow();

        run();

        assertEquals(1, f.users.findAll().size());
        assertEquals(first.getId(), f.users.findByEmail(EMAIL).orElseThrow().getId());
    }

    private void run() {
        seeder.run(new DefaultApplicationArguments());
    }
}
