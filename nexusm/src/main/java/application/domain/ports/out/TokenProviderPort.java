package application.domain.ports.out;

import application.domain.models.TokenClaims;
import java.util.Optional;

/**
 * Output port — signing and reading of JSON Web Tokens.
 */
public interface TokenProviderPort {

    /** Signs a JWT with the given data. */
    String issue(TokenClaims claims);

    /** Reads a JWT and verifies its signature; empty if it is invalid or expired. */
    Optional<TokenClaims> parse(String token);
}
