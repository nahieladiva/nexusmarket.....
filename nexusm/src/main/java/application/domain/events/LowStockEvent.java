package application.domain.events;

import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event raised when inventory falls below the reorder threshold.
 */
public record LowStockEvent(ProductId productId, WarehouseId warehouseId,
                            Quantity onHand, Quantity reorderThreshold,
                            LocalDateTime occurredAt) implements DomainEvent {

    public LowStockEvent {
        Objects.requireNonNull(productId, "productId is required");
        Objects.requireNonNull(warehouseId, "warehouseId is required");
        Objects.requireNonNull(onHand, "onHand is required");
        Objects.requireNonNull(reorderThreshold, "reorderThreshold is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }
}