package application.domain.events;

import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando se actualiza el inventario (entrada o salida de stock).
 */
public record InventoryUpdatedEvent(ProductId productId, WarehouseId warehouseId,
                                    Quantity newOnHand,
                                    LocalDateTime occurredAt) implements DomainEvent {

    public InventoryUpdatedEvent {
        Objects.requireNonNull(productId, "productId es obligatorio");
        Objects.requireNonNull(warehouseId, "warehouseId es obligatorio");
        Objects.requireNonNull(newOnHand, "newOnHand es obligatorio");
        Objects.requireNonNull(occurredAt, "occurredAt es obligatorio");
    }
}