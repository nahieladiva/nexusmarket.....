package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of a product (UUID).
 */
public final class ProductId {

    private final UUID value;

    public ProductId(UUID value) {
        this.value = Objects.requireNonNull(value, "Product id cannot be null");
    }

    public static ProductId of(UUID value) {
        return new ProductId(value);
    }

    public static ProductId of(String value) {
        return new ProductId(UUID.fromString(value));
    }

    public static ProductId random() {
        return new ProductId(UUID.randomUUID());
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductId productId = (ProductId) o;
        return value.equals(productId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}