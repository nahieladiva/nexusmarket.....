package application.domain.ports.out;

import application.domain.models.IssuedToken;
import application.domain.valueobjects.UserId;
import java.util.Optional;

/**
 * Output port — issued token database (MongoDB with a TTL index).
 */
public interface TokenStorePort {

    IssuedToken save(IssuedToken token);

    Optional<IssuedToken> findById(String tokenId);

    /** Revokes every active token of the user, so that all their sessions are closed. */
    void revokeAllForUser(UserId userId);
}
