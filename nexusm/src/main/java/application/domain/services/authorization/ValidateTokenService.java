package application.domain.services.authorization;

import application.domain.exceptions.InvalidTokenException;
import application.domain.models.IssuedToken;
import application.domain.models.TokenClaims;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.TokenProviderPort;
import application.domain.ports.out.TokenStorePort;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * Validates a JWT: its signature and expiration, and that it was not revoked.
 */
@DomainService
public class ValidateTokenService {

    private final TokenProviderPort tokenProvider;
    private final TokenStorePort tokenStore;
    private final ClockPort clock;

    public ValidateTokenService(TokenProviderPort tokenProvider, TokenStorePort tokenStore,
                                ClockPort clock) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider);
        this.tokenStore = Objects.requireNonNull(tokenStore);
        this.clock = Objects.requireNonNull(clock);
    }

    public TokenClaims execute(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Token is required");
        }
        TokenClaims claims = tokenProvider.parse(token)
            .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));
        IssuedToken stored = tokenStore.findById(claims.tokenId())
            .orElseThrow(() -> new InvalidTokenException("Token is not registered"));
        if (!stored.isUsableAt(clock.now())) {
            throw new InvalidTokenException("Token was revoked or expired");
        }
        return claims;
    }
}
