package application.domain.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidTokenException;
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
    void changingThePasswordClosesEverySessionOfTheUser() {
        String first = f.login.execute(EMAIL, "Password2026").token();
        String second = f.login.execute(EMAIL, "Password2026").token();

        f.changePassword.execute(user, "Password2026", "NewPassword2026");

        assertThrows(InvalidTokenException.class, () -> f.validateToken.execute(first));
        assertThrows(InvalidTokenException.class, () -> f.validateToken.execute(second));
    }

    @Test
    void changingThePasswordKeepsTheSessionsOfOtherUsers() {
        Email otherEmail = Email.of("luis@mail.com");
        User other = f.registerBuyer.execute("Luis", otherEmail, PhoneNumber.of("+573001112244"), "Password2026",
            TestFixture.address());
        String otherToken = f.login.execute(otherEmail, "Password2026").token();

        f.changePassword.execute(user, "Password2026", "NewPassword2026");

        assertEquals(other.getId(), f.validateToken.execute(otherToken).userId());
    }

    @Test
    void aFailedChangeKeepsTheSessionsOpen() {
        String token = f.login.execute(EMAIL, "Password2026").token();

        assertThrows(InvalidCredentialsException.class,
            () -> f.changePassword.execute(user, "Wrong2026", "NewPassword2026"));

        assertEquals(user.getId(), f.validateToken.execute(token).userId());
    }

    @Test
    void aNewLoginWorksAfterChangingThePassword() {
        f.changePassword.execute(user, "Password2026", "NewPassword2026");

        String token = f.login.execute(EMAIL, "NewPassword2026").token();

        assertEquals(user.getId(), f.validateToken.execute(token).userId());
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
