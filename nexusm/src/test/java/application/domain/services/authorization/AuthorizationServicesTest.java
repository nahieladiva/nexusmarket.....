package application.domain.services.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.UserRole;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidTokenException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.AuthSession;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import application.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of authentication and of the actor resolution.
 */
class AuthorizationServicesTest {

    private static final Email EMAIL = Email.of("ana@mail.com");

    private TestFixture f;
    private User buyer;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
        buyer = f.registerBuyer.execute("Ana", EMAIL, PhoneNumber.of("+573001112233"), "Password2026",
            TestFixture.address());
    }

    @Test
    void loginIssuesAValidToken() {
        AuthSession session = f.login.execute(EMAIL, "Password2026");
        TokenClaims claims = f.validateToken.execute(session.token());
        assertEquals(buyer.getId(), claims.userId());
        assertEquals(UserRole.BUYER, claims.role());
    }

    @Test
    void wrongCredentialsAreRejected() {
        assertThrows(InvalidCredentialsException.class, () -> f.login.execute(EMAIL, "Wrong2026"));
        assertThrows(InvalidCredentialsException.class,
            () -> f.login.execute(Email.of("nobody@mail.com"), "Password2026"));
    }

    @Test
    void logoutRevokesTheToken() {
        String token = f.login.execute(EMAIL, "Password2026").token();
        f.logout.execute(token);
        assertThrows(InvalidTokenException.class, () -> f.validateToken.execute(token));
    }

    @Test
    void blankOrUnknownTokenIsRejected() {
        assertThrows(InvalidTokenException.class, () -> f.validateToken.execute(" "));
        assertThrows(InvalidTokenException.class, () -> f.validateToken.execute("unknown"));
    }

    @Test
    void actorIsResolvedFromItsId() {
        assertEquals(buyer.getId(), f.resolveActor.execute(buyer.getId()).getId());
        assertThrows(ResourceNotFoundException.class, () -> f.resolveActor.execute(UserId.random()));
    }
}
