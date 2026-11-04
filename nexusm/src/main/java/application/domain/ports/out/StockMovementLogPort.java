package application.domain.ports.out;

import application.domain.models.StockMovement;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;
import java.util.List;

/**
 * Output port for the stock movement history (stored in MongoDB).
 */
public interface StockMovementLogPort {

    void record(StockMovement movement);

    /** Movements of a product in a warehouse, from the most recent to the oldest. */
    List<StockMovement> findByProductAndWarehouse(ProductId productId, WarehouseId warehouseId);
}
