package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of an order (UUID).
 */
public final class OrderId {

    private final UUID value;

    public OrderId(UUID value) {
        this.value = Objects.requireNonNull(value, "Order id cannot be null");
    }

    public static OrderId of(UUID value) {
        return new OrderId(value);
    }

    public static OrderId of(String value) {
        return new OrderId(UUID.fromString(value));
    }

    public static OrderId random() {
        return new OrderId(UUID.randomUUID());
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
        OrderId orderId = (OrderId) o;
        return value.equals(orderId.value);
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