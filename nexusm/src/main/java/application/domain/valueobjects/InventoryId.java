package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of an inventory record (UUID).
 */
public final class InventoryId {

    private final UUID value;

    public InventoryId(UUID value) {
        this.value = Objects.requireNonNull(value, "Inventory id cannot be null");
    }

    public static InventoryId of(UUID value) {
        return new InventoryId(value);
    }

    public static InventoryId of(String value) {
        return new InventoryId(UUID.fromString(value));
    }

    public static InventoryId random() {
        return new InventoryId(UUID.randomUUID());
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
        InventoryId that = (InventoryId) o;
        return value.equals(that.value);
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