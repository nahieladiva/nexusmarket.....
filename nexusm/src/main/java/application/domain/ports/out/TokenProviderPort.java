package application.domain.ports.out;

import application.domain.models.TokenClaims;
import java.util.Optional;

/**
 * Puerto de salida 9/15 — firma y lectura de JSON Web Tokens.
 */
public interface TokenProviderPort {

    /** Firma un JWT con los datos dados. */
    String issue(TokenClaims claims);

    /** Lee y verifica la firma de un JWT; vacío si es inválido o expiró. */
    Optional<TokenClaims> parse(String token);
}
