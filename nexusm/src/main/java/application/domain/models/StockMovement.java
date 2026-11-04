package application.domain.models;

import application.domain.enums.StockMovementType;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro inmutable de un movimiento de stock.
 *
 * @param delta         cambio aplicado (positivo = entrada, negativo = salida)
 * @param resultingOnHand stock que quedó después del movimiento
 * @param performedBy   usuario que lo hizo; {@code null} si lo hizo el sistema
 * @param reference     referencia opcional (por ejemplo, el id del pedido)
 */
public record StockMovement(String id, ProductId productId, WarehouseId warehouseId,
                            StockMovementType type, int delta, Quantity resultingOnHand,
                            String reason, UserId performedBy, String reference,
                            LocalDateTime occurredAt) {

    public StockMovement {
        Objects.requireNonNull(id, "El id es obligatorio");
        Objects.requireNonNull(productId, "El producto es obligatorio");
        Objects.requireNonNull(warehouseId, "La bodega es obligatoria");
        Objects.requireNonNull(type, "El tipo es obligatorio");
        Objects.requireNonNull(resultingOnHand, "El stock resultante es obligatorio");
        Objects.requireNonNull(occurredAt, "La fecha es obligatoria");
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Todo movimiento de stock debe tener un motivo");
        }
    }
}
