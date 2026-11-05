package application.infrastructure.database;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * MongoDB configuration.
 *
 * <p>Creates a TTL index on {@code expiresAt} in the token collection: MongoDB deletes each
 * token when it expires, so the token database does not grow without limit.</p>
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public ApplicationRunner tokenExpirationIndex(MongoTemplate mongoTemplate) {
        return args -> mongoTemplate.getCollection(IssuedTokenDocument.COLLECTION)
            .createIndex(Indexes.ascending("expiresAt"),
                new IndexOptions().name("ttl_expiresAt").expireAfter(0L, TimeUnit.SECONDS));
    }
}
