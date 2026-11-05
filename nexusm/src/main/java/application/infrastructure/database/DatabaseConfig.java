package application.infrastructure.database;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB configuration.
 *
 * <p>Creates a TTL index on {@code expiresAt} in the token collection: MongoDB deletes each
 * token when it expires, so the token database does not grow without limit. It also indexes
 * {@code userId}, because all the tokens of a user are revoked when the password changes.</p>
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public ApplicationRunner tokenIndexes(MongoTemplate mongoTemplate) {
        return args -> {
            MongoCollection<Document> tokens = mongoTemplate.getCollection(IssuedTokenDocument.COLLECTION);
            tokens.createIndex(Indexes.ascending("expiresAt"),
                new IndexOptions().name("ttl_expiresAt").expireAfter(0L, TimeUnit.SECONDS));
            tokens.createIndex(Indexes.ascending("userId"), new IndexOptions().name("idx_userId"));
        };
    }
}
