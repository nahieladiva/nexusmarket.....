package application.domain.events;

import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event raised when inventory is updated (stock coming in or going out).
 */
public record InventoryUpdatedEvent(ProductId productId, WarehouseId warehouseId,
                                    Quantity newOnHand,
                                    LocalDateTime occurredAt) implements DomainEvent {

    public InventoryUpdatedEvent {
        Objects.requireNonNull(productId, "productId is required");
        Objects.requireNonNull(warehouseId, "warehouseId is required");
        Objects.requireNonNull(newOnHand, "newOnHand is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }
}