package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.domain.enums.TokenStatus;
import application.domain.models.IssuedToken;
import application.domain.ports.out.TokenStorePort;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * MongoDB adapter of {@link TokenStorePort} (collection {@code issued_tokens} with a TTL index).
 */
@Component
public class TokenStoreAdapter implements TokenStorePort {

    private final IssuedTokenMongoRepository repository;
    private final ZoneId zone = ZoneId.systemDefault();

    public TokenStoreAdapter(IssuedTokenMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "Repository is required");
    }

    @Override
    public IssuedToken save(IssuedToken token) {
        IssuedTokenDocument doc = new IssuedTokenDocument();
        doc.setTokenId(token.getTokenId());
        doc.setUserId(token.getUserId().toString());
        doc.setIssuedAt(token.getIssuedAt());
        doc.setExpiresAt(Date.from(token.getExpiresAt().atZone(zone).toInstant()));
        doc.setStatus(token.getStatus());
        repository.save(doc);
        return token;
    }

    @Override
    public Optional<IssuedToken> findById(String tokenId) {
        return repository.findById(tokenId).map(doc -> new IssuedToken(doc.getTokenId(),
            UserId.of(doc.getUserId()), doc.getIssuedAt(),
            LocalDateTime.ofInstant(doc.getExpiresAt().toInstant(), zone), toStatus(doc.getStatus())));
    }

    @Override
    public void revokeAllForUser(UserId userId) {
        List<IssuedTokenDocument> active =
            repository.findByUserIdAndStatus(userId.toString(), TokenStatus.ACTIVE);
        active.forEach(doc -> doc.setStatus(TokenStatus.REVOKED));
        repository.saveAll(active);
    }

    /**
     * Documents saved before the status field existed have no status; they are treated
     * as revoked so the user simply logs in again.
     */
    private TokenStatus toStatus(TokenStatus value) {
        return value == null ? TokenStatus.REVOKED : value;
    }
}
