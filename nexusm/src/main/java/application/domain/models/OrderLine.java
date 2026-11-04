package application.domain.models;

import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import java.util.Objects;

/**
 * Line requested by the buyer when creating an order: only product and quantity.
 * The client NEVER sends the price; the system calculates it from the catalog.
 */
public record OrderLine(ProductId productId, Quantity quantity) {

    public OrderLine {
        Objects.requireNonNull(productId, "Product is required");
        Objects.requireNonNull(quantity, "Quantity is required");
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }
}
