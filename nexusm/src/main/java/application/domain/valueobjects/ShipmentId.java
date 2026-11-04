package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of a shipment (UUID).
 */
public final class ShipmentId {

    private final UUID value;

    public ShipmentId(UUID value) {
        this.value = Objects.requireNonNull(value, "Shipment id cannot be null");
    }

    public static ShipmentId of(UUID value) {
        return new ShipmentId(value);
    }

    public static ShipmentId of(String value) {
        return new ShipmentId(UUID.fromString(value));
    }

    public static ShipmentId random() {
        return new ShipmentId(UUID.randomUUID());
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
        ShipmentId shipmentId = (ShipmentId) o;
        return value.equals(shipmentId.value);
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
