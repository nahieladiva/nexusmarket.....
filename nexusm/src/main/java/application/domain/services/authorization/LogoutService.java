package application.domain.services.authorization;

import application.domain.exceptions.InvalidTokenException;
import application.domain.models.IssuedToken;
import application.domain.models.TokenClaims;
import application.domain.ports.out.TokenStorePort;
import application.domain.services.DomainService;

import java.util.Objects;

/**
 * Closes a session: the JWT is revoked and is rejected even if it has not expired.
 */
@DomainService
public class LogoutService {

    private final TokenStorePort tokenStore;
    private final ValidateTokenService validateTokenService;

    public LogoutService(TokenStorePort tokenStore, ValidateTokenService validateTokenService) {
        this.tokenStore = Objects.requireNonNull(tokenStore);
        this.validateTokenService = Objects.requireNonNull(validateTokenService);
    }

    public void execute(String token) {
        TokenClaims claims = validateTokenService.execute(token);
        IssuedToken stored = tokenStore.findById(claims.tokenId())
            .orElseThrow(() -> new InvalidTokenException("Token is not registered"));
        stored.revoke();
        tokenStore.save(stored);
    }
}
