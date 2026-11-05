package application.adapters.out.persistence.mongodb.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.domain.enums.TokenStatus;
import application.domain.valueobjects.UserId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Tests of the token store logic. {@code TokenStoreAdapterIT} runs it on MongoDB.
 */
class TokenStoreAdapterTest {

    private final IssuedTokenMongoRepository repository = mock(IssuedTokenMongoRepository.class);
    private final TokenStoreAdapter adapter = new TokenStoreAdapter(repository);

    @Test
    void revokesTheActiveTokensOfTheUser() {
        UserId userId = UserId.random();
        IssuedTokenDocument first = active();
        IssuedTokenDocument second = active();
        List<IssuedTokenDocument> documents = List.of(first, second);
        when(repository.findByUserIdAndStatus(userId.toString(), TokenStatus.ACTIVE))
            .thenReturn(documents);

        adapter.revokeAllForUser(userId);

        assertEquals(TokenStatus.REVOKED, first.getStatus());
        assertEquals(TokenStatus.REVOKED, second.getStatus());
        verify(repository).saveAll(documents);
    }

    @Test
    void aDocumentWithoutStatusIsTreatedAsRevoked() {
        IssuedTokenDocument document = new IssuedTokenDocument();
        document.setTokenId("old-token");
        document.setUserId(UserId.random().toString());
        document.setIssuedAt(java.time.LocalDateTime.of(2026, 1, 1, 10, 0));
        document.setExpiresAt(new Date(System.currentTimeMillis() + 3_600_000));
        when(repository.findById("old-token")).thenReturn(Optional.of(document));

        assertEquals(TokenStatus.REVOKED, adapter.findById("old-token").orElseThrow().getStatus());
    }

    private static IssuedTokenDocument active() {
        IssuedTokenDocument document = new IssuedTokenDocument();
        document.setStatus(TokenStatus.ACTIVE);
        return document;
    }
}
