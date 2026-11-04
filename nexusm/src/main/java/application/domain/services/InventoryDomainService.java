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
 * Servicio de dominio para la gestión y reserva de inventario.
 */
public final class InventoryDomainService {

    private final InventoryRepository inventoryRepository;

    public InventoryDomainService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository,
            "inventoryRepository es obligatorio");
    }

    /**
     * Verifica la disponibilidad de una cantidad de un producto en un almacén.
     */
    public boolean checkAvailability(ProductId productId, WarehouseId warehouseId,
                                     Quantity requested) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .map(inventory -> inventory.isAvailable(requested))
            .orElse(false);
    }

    /**
     * Reserva stock de un producto en un almacén (decrementa el disponible).
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
     * Reabastece stock de un producto en un almacén (incrementa el disponible).
     */
    public Inventory restock(ProductId productId, WarehouseId warehouseId, Quantity quantity) {
        Inventory inventory = findInventoryOrThrow(productId, warehouseId);
        inventory.increase(quantity);
        return inventoryRepository.save(inventory);
    }

    private Inventory findInventoryOrThrow(ProductId productId, WarehouseId warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventario para el producto " + productId + " en el almacén " + warehouseId,
                productId + "|" + warehouseId));
    }
}