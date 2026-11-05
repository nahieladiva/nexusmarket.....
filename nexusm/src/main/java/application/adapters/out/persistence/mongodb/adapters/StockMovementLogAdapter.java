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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * MongoDB adapter of {@link StockMovementLogPort}.
 *
 * <p>MongoDB does not take part in the MySQL transaction, so a movement is written only after that
 * transaction commits. If it rolls back, the history stays clean.</p>
 */
@Component
public class StockMovementLogAdapter implements StockMovementLogPort {

    private static final Logger log = LoggerFactory.getLogger(StockMovementLogAdapter.class);

    private final StockMovementMongoRepository repository;

    public StockMovementLogAdapter(StockMovementMongoRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository is required");
    }

    @Override
    public void record(StockMovement movement) {
        StockMovementDocument document = toDocument(movement);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            repository.save(document);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                saveAfterCommit(document);
            }
        });
    }

    /** The stock already changed in MySQL, so a failure here must not reach the client as an error. */
    private void saveAfterCommit(StockMovementDocument document) {
        try {
            repository.save(document);
        } catch (RuntimeException ex) {
            log.error("Stock movement {} was not recorded", document.getId(), ex);
        }
    }

    @Override
    public List<StockMovement> findByProductAndWarehouse(ProductId productId, WarehouseId warehouseId) {
        return repository.findByProductIdAndWarehouseIdOrderByOccurredAtDesc(
                productId.toString(), warehouseId.toString()).stream()
            .map(this::toDomain)
            .toList();
    }

    private StockMovementDocument toDocument(StockMovement movement) {
        StockMovementDocument document = new StockMovementDocument();
        document.setId(movement.id());
        document.setProductId(movement.productId().toString());
        document.setWarehouseId(movement.warehouseId().toString());
        document.setType(movement.type().name());
        document.setDelta(movement.delta());
        document.setResultingOnHand(movement.resultingOnHand().getValue());
        document.setReason(movement.reason());
        document.setPerformedBy(movement.performedBy() == null ? null : movement.performedBy().toString());
        document.setReference(movement.reference());
        document.setOccurredAt(movement.occurredAt());
        return document;
    }

    private StockMovement toDomain(StockMovementDocument doc) {
        return new StockMovement(doc.getId(), ProductId.of(doc.getProductId()),
            WarehouseId.of(doc.getWarehouseId()), StockMovementType.valueOf(doc.getType()),
            doc.getDelta(), Quantity.of(doc.getResultingOnHand()), doc.getReason(),
            doc.getPerformedBy() == null ? null : UserId.of(doc.getPerformedBy()),
            doc.getReference(), doc.getOccurredAt());
    }
}
