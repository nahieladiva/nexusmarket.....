package application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidTokenException;
import application.domain.models.AuthSession;
import application.domain.models.Seller;
import application.domain.models.TokenClaims;
import application.domain.models.User;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.PhoneNumber;
import application.support.TestFixture;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthenticationApplicationServiceTest {

    private TestFixture f;

    @BeforeEach
    void setUp() {
        f = new TestFixture();
    }

    private User registerBuyer(String email) {
        return f.auth.registerBuyer("Ana Gómez", Email.of(email), PhoneNumber.of("+573001112233"),
            "Clave2026", TestFixture.address());
    }

    @Test
    void registerBuyerHashesPassword() {
        User buyer = registerBuyer("ana@correo.com");
        assertEquals(UserRole.BUYER, buyer.getRole());
        assertNotEquals("Clave2026", buyer.getPasswordHash());
    }

    @Test
    void emailMustBeUnique() {
        registerBuyer("ana@correo.com");
        assertThrows(DuplicateResourceException.class, () -> registerBuyer("ANA@correo.com"));
    }

    @Test
    void weakPasswordIsRejected() {
        assertThrows(BusinessRuleException.class, () -> f.auth.registerBuyer("Ana", Email.of("a@b.co"),
            PhoneNumber.of("+573001112233"), "solotexto", TestFixture.address()));
        assertThrows(BusinessRuleException.class, () -> f.auth.registerBuyer("Ana", Email.of("a@b.co"),
            PhoneNumber.of("+573001112233"), "abc1", TestFixture.address()));
    }

    @Test
    void newSellerStartsUnapproved() {
        Seller seller = (Seller) f.auth.registerSeller("Luis", Email.of("luis@tienda.com"),
            PhoneNumber.of("+573001112233"), "Tienda2026", "Tienda Luis");
        assertFalse(seller.isApproved());
    }

    @Test
    void loginIssuesOneHourTokenAndStoresIt() {
        User buyer = registerBuyer("ana@correo.com");
        AuthSession session = f.auth.login(Email.of("ana@correo.com"), "Clave2026");
        assertEquals(TestFixture.NOW.plusHours(1), session.expiresAt());
        TokenClaims claims = f.auth.validateToken(session.token());
        assertEquals(buyer.getId(), claims.userId());
        assertTrue(f.tokenStore.findById(claims.tokenId()).isPresent());
    }

    @Test
    void wrongPasswordOrUnknownEmailFail() {
        registerBuyer("ana@correo.com");
        assertThrows(InvalidCredentialsException.class,
            () -> f.auth.login(Email.of("ana@correo.com"), "OtraClave1"));
        assertThrows(InvalidCredentialsException.class,
            () -> f.auth.login(Email.of("nadie@correo.com"), "Clave2026"));
    }

    @Test
    void logoutRevokesTokenBeforeExpiration() {
        registerBuyer("ana@correo.com");
        AuthSession session = f.auth.login(Email.of("ana@correo.com"), "Clave2026");
        f.auth.logout(session.token());
        assertThrows(InvalidTokenException.class, () -> f.auth.validateToken(session.token()));
    }

    @Test
    void tokenExpiresAfterOneHour() {
        registerBuyer("ana@correo.com");
        AuthSession session = f.auth.login(Email.of("ana@correo.com"), "Clave2026");
        f.clock.advance(Duration.ofMinutes(61));
        assertThrows(InvalidTokenException.class, () -> f.auth.validateToken(session.token()));
    }
}
