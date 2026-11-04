package application.adapters.out.persistence.mongodb.repositories;

import application.adapters.out.persistence.mongodb.documents.StockMovementDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockMovementMongoRepository extends MongoRepository<StockMovementDocument, String> {

    List<StockMovementDocument> findByProductIdAndWarehouseIdOrderByOccurredAtDesc(String productId,
                                                                                    String warehouseId);
}
