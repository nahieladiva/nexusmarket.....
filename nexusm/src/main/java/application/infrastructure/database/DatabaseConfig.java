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
 * Configuración de MongoDB.
 *
 * <p>Crea un índice TTL sobre {@code expiresAt} en la colección de tokens: MongoDB borra
 * solo cada token cuando expira, así la base de tokens no crece sin límite.</p>
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
