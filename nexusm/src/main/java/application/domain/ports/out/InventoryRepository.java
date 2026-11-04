package application.domain.ports.out;

import application.domain.models.Inventory;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — stock by product and warehouse (MySQL).
 */
public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(application.domain.valueobjects.InventoryId id);

    Optional<Inventory> findByProductIdAndWarehouseId(ProductId productId, WarehouseId warehouseId);

    List<Inventory> findByWarehouseId(WarehouseId warehouseId);

    List<Inventory> findByProductId(ProductId productId);

    List<Inventory> findAll();
}