package application.domain.ports.out;

import application.domain.models.StockMovement;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;
import java.util.List;

/**
 * Puerto de salida 8/15 — historial de movimientos de stock (MongoDB).
 */
public interface StockMovementLogPort {

    void record(StockMovement movement);

    /** Movimientos de un producto en una bodega, del más reciente al más antiguo. */
    List<StockMovement> findByProductAndWarehouse(ProductId productId, WarehouseId warehouseId);
}
