package application.domain.models;

import application.domain.enums.StockMovementType;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable record of a stock movement.
 *
 * @param delta           applied change (positive = inbound, negative = outbound)
 * @param resultingOnHand stock left after the movement
 * @param performedBy     user who made it; {@code null} when the system made it
 * @param reference       optional reference (for example, the order id)
 */
public record StockMovement(String id, ProductId productId, WarehouseId warehouseId,
                            StockMovementType type, int delta, Quantity resultingOnHand,
                            String reason, UserId performedBy, String reference,
                            LocalDateTime occurredAt) {

    public StockMovement {
        Objects.requireNonNull(id, "Id is required");
        Objects.requireNonNull(productId, "Product is required");
        Objects.requireNonNull(warehouseId, "Warehouse is required");
        Objects.requireNonNull(type, "Movement type is required");
        Objects.requireNonNull(resultingOnHand, "Resulting stock is required");
        Objects.requireNonNull(occurredAt, "Date is required");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Every stock movement must have a reason");
        }
        if (!type.allowsAnySign() && delta != 0 && (delta > 0) != type.isInbound()) {
            throw new IllegalArgumentException(
                "The sign of the movement does not match its type " + type);
        }
    }
}
