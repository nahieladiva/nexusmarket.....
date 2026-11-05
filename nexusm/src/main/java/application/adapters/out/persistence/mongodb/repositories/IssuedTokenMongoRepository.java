package application.adapters.out.persistence.mongodb.repositories;

import application.adapters.out.persistence.mongodb.documents.IssuedTokenDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IssuedTokenMongoRepository extends MongoRepository<IssuedTokenDocument, String> {

    List<IssuedTokenDocument> findByUserIdAndStatus(String userId, String status);
}
