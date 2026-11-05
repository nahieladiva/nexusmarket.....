package application.adapters.out.persistence.mongodb.adapters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import application.adapters.out.persistence.mongodb.documents.StockMovementDocument;
import application.adapters.out.persistence.mongodb.repositories.StockMovementMongoRepository;
import application.domain.enums.StockMovementType;
import application.domain.models.StockMovement;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.support.TestFixture;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The history in MongoDB only receives what the MySQL transaction committed.
 */
class StockMovementLogAdapterTest {

    private final StockMovementMongoRepository repository = mock(StockMovementMongoRepository.class);
    private final StockMovementLogAdapter adapter = new StockMovementLogAdapter(repository);
    private final TransactionTemplate transaction = new TransactionTemplate(new NoOpTransactionManager());

    private static StockMovement movement() {
        return new StockMovement("movement-id", ProductId.random(), WarehouseId.random(),
            StockMovementType.RESTOCK, 5, Quantity.of(15), "Purchase", UserId.random(), null,
            TestFixture.NOW);
    }

    @Test
    void outsideATransactionItIsWrittenAtOnce() {
        adapter.record(movement());

        verify(repository, times(1)).save(any(StockMovementDocument.class));
    }

    @Test
    void insideATransactionItWaitsForTheCommit() {
        transaction.executeWithoutResult(status -> {
            adapter.record(movement());
            verify(repository, never()).save(any(StockMovementDocument.class));
        });

        verify(repository, times(1)).save(any(StockMovementDocument.class));
    }

    @Test
    void aRollbackLeavesNoHistory() {
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            adapter.record(movement());
            throw new IllegalStateException("The payment failed");
        }));

        verify(repository, never()).save(any(StockMovementDocument.class));
    }

    @Test
    void aFailureAfterTheCommitDoesNotReachTheClient() {
        when(repository.save(any(StockMovementDocument.class)))
            .thenThrow(new IllegalStateException("MongoDB is down"));

        assertDoesNotThrow(() -> transaction.executeWithoutResult(status -> adapter.record(movement())));
    }

    /** A transaction manager without a resource: it only runs the synchronizations. */
    private static final class NoOpTransactionManager extends AbstractPlatformTransactionManager
            implements PlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            // Nothing to open.
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            // Nothing to commit.
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            // Nothing to roll back.
        }
    }
}
