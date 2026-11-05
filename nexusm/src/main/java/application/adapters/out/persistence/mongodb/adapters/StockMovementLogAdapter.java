package application.adapters.out.persistence.mongodb.adapters;

import application.adapters.out.persistence.mongodb.documents.StockMovementDocument;
import application.adapters.out.persistence.mongodb.repositories.StockMovementMongoRepository;
import application.domain.enums.StockMovementType;
import application.domain.models.StockMovement;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * MongoDB adapter of {@link StockMovementLogPort}.
 */
@Component
public class StockMovementLogAdapter implements StockMovementLogPort {

    private final StockMovementMongoRepository repository;

    public StockMovementLogAdapter(StockMovementMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    @Override
    public void record(StockMovement movement) {
        StockMovementDocument doc = new StockMovementDocument();
        doc.setId(movement.id());
        doc.setProductId(movement.productId().toString());
        doc.setWarehouseId(movement.warehouseId().toString());
        doc.setType(movement.type().name());
        doc.setDelta(movement.delta());
        doc.setResultingOnHand(movement.resultingOnHand().getValue());
        doc.setReason(movement.reason());
        doc.setPerformedBy(movement.performedBy() == null ? null : movement.performedBy().toString());
        doc.setReference(movement.reference());
        doc.setOccurredAt(movement.occurredAt());
        repository.save(doc);
    }

    @Override
    public List<StockMovement> findByProductAndWarehouse(ProductId productId, WarehouseId warehouseId) {
        return repository.findByProductIdAndWarehouseIdOrderByOccurredAtDesc(
                productId.toString(), warehouseId.toString()).stream()
            .map(this::toDomain)
            .toList();
    }

    private StockMovement toDomain(StockMovementDocument doc) {
        return new StockMovement(doc.getId(), ProductId.of(doc.getProductId()),
            WarehouseId.of(doc.getWarehouseId()), StockMovementType.valueOf(doc.getType()),
            doc.getDelta(), Quantity.of(doc.getResultingOnHand()), doc.getReason(),
            doc.getPerformedBy() == null ? null : UserId.of(doc.getPerformedBy()),
            doc.getReference(), doc.getOccurredAt());
    }
}
