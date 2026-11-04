package application.domain.ports.in;

import application.domain.models.Inventory;
import application.domain.models.StockMovement;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;
import java.util.List;

/**
 * Puerto de entrada 5/7 — stock por producto y bodega.
 *
 * <p>Las operaciones de escritura solo las puede hacer el vendedor dueño del producto
 * o un administrador ({@code actorId}).</p>
 */
public interface InventoryManagementPort {

    /** Registra el stock inicial. Un solo registro por producto y bodega; bodega activa. */
    Inventory createInventory(UserId actorId, ProductId productId, WarehouseId warehouseId,
                              Quantity onHand, Quantity reorderThreshold,
                              WarehouseLocation location);

    /** Entrada de mercancía. */
    Inventory restock(UserId actorId, ProductId productId, WarehouseId warehouseId,
                      Quantity amount, String reason);

    /** Ajuste manual positivo o negativo. El stock nunca queda negativo. */
    Inventory adjustStock(UserId actorId, ProductId productId, WarehouseId warehouseId,
                          int delta, String reason);

    Inventory changeReorderThreshold(UserId actorId, ProductId productId,
                                     WarehouseId warehouseId, Quantity newThreshold);

    Inventory findInventory(ProductId productId, WarehouseId warehouseId);

    boolean isAvailable(ProductId productId, WarehouseId warehouseId, Quantity requested);

    List<Inventory> findByWarehouse(WarehouseId warehouseId);

    List<Inventory> findAllInventory();

    /** Inventarios por debajo de su umbral de reorden. */
    List<Inventory> findBelowReorderPoint();

    /** Historial de movimientos, del más reciente al más antiguo. */
    List<StockMovement> findMovements(ProductId productId, WarehouseId warehouseId);
}
