package application.domain.services;

import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.ports.out.InventoryRepository;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Domain service for inventory management and reservation.
 */
public final class InventoryDomainService {

    private final InventoryRepository inventoryRepository;

    public InventoryDomainService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository,
            "inventoryRepository is required");
    }

    /**
     * Checks the availability of a quantity of a product in a warehouse.
     */
    public boolean checkAvailability(ProductId productId, WarehouseId warehouseId,
                                     Quantity requested) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(inventory -> inventory.isAvailable(requested))
            .orElse(false);
    }

    /**
     * Reserves stock of a product in a warehouse (decreases the available stock).
     */
    public Inventory reserve(ProductId productId, WarehouseId warehouseId, Quantity quantity) {
        Inventory inventory = findInventoryOrThrow(productId, warehouseId);
        if (!inventory.isAvailable(quantity)) {
            throw new InsufficientStockException(
                productId, warehouseId, quantity, inventory.getOnHand());
        }
        inventory.decrease(quantity);
        return inventoryRepository.save(inventory);
    }

    /**
     * Restocks a product in a warehouse (increases the available stock).
     */
    public Inventory restock(ProductId productId, WarehouseId warehouseId, Quantity quantity) {
        Inventory inventory = findInventoryOrThrow(productId, warehouseId);
        inventory.increase(quantity);
        return inventoryRepository.save(inventory);
    }

    private Inventory findInventoryOrThrow(ProductId productId, WarehouseId warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventory for product " + productId + " in warehouse " + warehouseId,
                productId + "|" + warehouseId));
    }
}