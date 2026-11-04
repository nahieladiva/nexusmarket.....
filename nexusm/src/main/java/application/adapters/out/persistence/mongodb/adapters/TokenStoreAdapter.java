package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.domain.models.IssuedToken;
import application.domain.ports.out.TokenStorePort;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador MongoDB de {@link TokenStorePort} (colección {@code issued_tokens} con TTL).
 */
@Component
public class TokenStoreAdapter implements TokenStorePort {

    private final IssuedTokenMongoRepository repository;
    private final ZoneId zone = ZoneId.systemDefault();

    public TokenStoreAdapter(IssuedTokenMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository es obligatorio");
    }

    @Override
    public IssuedToken save(IssuedToken token) {
        IssuedTokenDocument doc = new IssuedTokenDocument();
        doc.setTokenId(token.getTokenId());
        doc.setUserId(token.getUserId().toString());
        doc.setIssuedAt(token.getIssuedAt());
        doc.setExpiresAt(Date.from(token.getExpiresAt().atZone(zone).toInstant()));
        doc.setRevoked(token.isRevoked());
        repository.save(doc);
        return token;
    }

    @Override
    public Optional<IssuedToken> findById(String tokenId) {
        return repository.findById(tokenId).map(doc -> new IssuedToken(doc.getTokenId(),
            UserId.of(doc.getUserId()), doc.getIssuedAt(),
            LocalDateTime.ofInstant(doc.getExpiresAt().toInstant(), zone), doc.isRevoked()));
    }
}
