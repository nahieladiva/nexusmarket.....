package application.domain.ports.out;

import application.domain.models.IssuedToken;
import java.util.Optional;

/**
 * Output port 10/15 — issued token database (MongoDB with a TTL index).
 */
public interface TokenStorePort {

    IssuedToken save(IssuedToken token);

    Optional<IssuedToken> findById(String tokenId);
}
