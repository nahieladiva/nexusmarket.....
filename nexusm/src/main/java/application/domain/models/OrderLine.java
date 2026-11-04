package application.domain.models;

import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import java.util.Objects;

/**
 * Línea solicitada por el comprador al crear un pedido: solo producto y cantidad.
 * El precio NUNCA lo envía el cliente; lo calcula el sistema a partir del catálogo.
 */
public record OrderLine(ProductId productId, Quantity quantity) {

    public OrderLine {
        Objects.requireNonNull(productId, "El producto es obligatorio");
        Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }
}
