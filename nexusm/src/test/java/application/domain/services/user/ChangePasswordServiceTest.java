package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the password change.
 */
class ChangePasswordServiceTest {

    private static final Email EMAIL = Email.of("ana@mail.com");

    private TestFixture f;
    private User user;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        user = f.registerBuyer.execute("Ana", EMAIL, PhoneNumber.of("+573001112233"), "Password2026",
            TestFixture.address());
    }

    @Test
    void newPasswordReplacesTheCurrentOne() {
        f.changePassword.execute(user, "Password2026", "NewPassword2026");
        assertEquals(user.getId(), f.login.execute(EMAIL, "NewPassword2026").user().getId());
        assertThrows(InvalidCredentialsException.class, () -> f.login.execute(EMAIL, "Password2026"));
    }

    @Test
    void currentPasswordIsRequired() {
        assertThrows(InvalidCredentialsException.class,
            () -> f.changePassword.execute(user, "Wrong2026", "NewPassword2026"));
        assertThrows(InvalidCredentialsException.class,
            () -> f.changePassword.execute(user, null, "NewPassword2026"));
    }

    @Test
    void newPasswordMustFollowThePolicy() {
        assertThrows(BusinessRuleException.class, () -> f.changePassword.execute(user, "Password2026", "short"));
    }
}
