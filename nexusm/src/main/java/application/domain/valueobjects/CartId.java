package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of a shopping cart (UUID).
 */
public final class CartId {

    private final UUID value;

    public CartId(UUID value) {
        this.value = Objects.requireNonNull(value, "Cart id cannot be null");
    }

    public static CartId of(UUID value) {
        return new CartId(value);
    }

    public static CartId of(String value) {
        return new CartId(UUID.fromString(value));
    }

    public static CartId random() {
        return new CartId(UUID.randomUUID());
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
        CartId cartId = (CartId) o;
        return value.equals(cartId.value);
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
