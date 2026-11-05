package application.adapters.out.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.UserRole;
import application.domain.models.TokenClaims;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests of the JWT provider: the token carries the role of the user and cannot be forged.
 */
class JjwtTokenProviderAdapterTest {

    private static final String SECRET = "test-secret-with-at-least-32-characters";

    private final JjwtTokenProviderAdapter provider = new JjwtTokenProviderAdapter(SECRET);

    @ParameterizedTest
    @EnumSource(UserRole.class)
    void everyRoleTravelsInTheToken(UserRole role) {
        TokenClaims claims = claims(role, 1);

        assertEquals(claims, provider.parse(provider.issue(claims)).orElseThrow());
    }

    @Test
    void anExpiredTokenIsRejected() {
        TokenClaims expired = new TokenClaims("token-id", UserId.random(), UserRole.BUYER,
            now().minusHours(2), now().minusHours(1));

        assertTrue(provider.parse(provider.issue(expired)).isEmpty());
    }

    @Test
    void aTokenSignedWithAnotherKeyIsRejected() {
        JjwtTokenProviderAdapter other =
            new JjwtTokenProviderAdapter("another-secret-with-at-least-32-characters");

        assertTrue(provider.parse(other.issue(claims(UserRole.ADMINISTRATOR, 1))).isEmpty());
    }

    @Test
    void aMalformedTokenIsRejected() {
        assertTrue(provider.parse("not-a-token").isEmpty());
    }

    @Test
    void aShortSecretIsRejected() {
        assertThrows(IllegalStateException.class, () -> new JjwtTokenProviderAdapter("short"));
    }

    private static TokenClaims claims(UserRole role, int hours) {
        return new TokenClaims("token-id", UserId.random(), role, now(), now().plusHours(hours));
    }

    private static LocalDateTime now() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
