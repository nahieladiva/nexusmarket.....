package application.domain.ports.out;

import application.domain.models.IssuedToken;
import java.util.Optional;

/**
 * Puerto de salida 10/15 — base de datos de tokens emitidos (MongoDB con índice TTL).
 */
public interface TokenStorePort {

    IssuedToken save(IssuedToken token);

    Optional<IssuedToken> findById(String tokenId);
}
