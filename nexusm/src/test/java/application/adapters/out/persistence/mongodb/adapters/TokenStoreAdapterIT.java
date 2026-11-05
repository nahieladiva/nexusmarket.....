package application.adapters.out.persistence.mongodb.adapters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import application.adapters.out.persistence.mongodb.repositories.IssuedTokenMongoRepository;
import application.domain.enums.TokenStatus;
import application.domain.models.IssuedToken;
import application.domain.valueobjects.UserId;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;

/**
 * Runs the token store on a real MongoDB, in a temporary database that is dropped at the end.
 *
 * <p>{@code mvnw test} does not run it. It needs {@code docker compose up -d mongo}, and then:
 * {@code mvnw test -Dtest=TokenStoreAdapterIT}.</p>
 */
class TokenStoreAdapterIT {

    private static final String DATABASE = "nexusmarket_it";

    private MongoClient client;
    private TokenStoreAdapter adapter;

    @BeforeEach
    void setUp() {
        client = MongoClients.create("mongodb://localhost:27017");
        MongoTemplate template = new MongoTemplate(client, DATABASE);
        adapter = new TokenStoreAdapter(
            new MongoRepositoryFactory(template).getRepository(IssuedTokenMongoRepository.class));
    }

    @AfterEach
    void tearDown() {
        client.getDatabase(DATABASE).drop();
        client.close();
    }

    @Test
    void revokesEveryActiveTokenOfTheUserOnly() {
        UserId user = UserId.random();
        UserId other = UserId.random();
        adapter.save(token("first", user));
        adapter.save(token("second", user));
        adapter.save(token("other", other));

        adapter.revokeAllForUser(user);

        assertEquals(TokenStatus.REVOKED, status("first"));
        assertEquals(TokenStatus.REVOKED, status("second"));
        assertEquals(TokenStatus.ACTIVE, status("other"));
    }

    @Test
    void aUserWithoutTokensChangesNothing() {
        adapter.save(token("other", UserId.random()));

        adapter.revokeAllForUser(UserId.random());

        assertEquals(TokenStatus.ACTIVE, status("other"));
    }

    private TokenStatus status(String tokenId) {
        return adapter.findById(tokenId).orElseThrow().getStatus();
    }

    private static IssuedToken token(String tokenId, UserId userId) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return IssuedToken.issue(tokenId, userId, now, now.plusHours(1));
    }
}
