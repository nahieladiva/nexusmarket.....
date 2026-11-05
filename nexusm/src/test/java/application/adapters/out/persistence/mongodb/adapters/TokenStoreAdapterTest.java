package application.adapters.out.persistence.mongodb.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.domain.enums.TokenStatus;
import application.domain.valueobjects.UserId;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests of the revocation logic of the token store. {@code TokenStoreAdapterIT} runs it on MongoDB.
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
        when(repository.findByUserIdAndStatus(userId.toString(), TokenStatus.ACTIVE.name()))
            .thenReturn(documents);

        adapter.revokeAllForUser(userId);

        assertEquals(TokenStatus.REVOKED.name(), first.getStatus());
        assertEquals(TokenStatus.REVOKED.name(), second.getStatus());
        verify(repository).saveAll(documents);
    }

    private static IssuedTokenDocument active() {
        IssuedTokenDocument document = new IssuedTokenDocument();
        document.setStatus(TokenStatus.ACTIVE.name());
        return document;
    }
}
