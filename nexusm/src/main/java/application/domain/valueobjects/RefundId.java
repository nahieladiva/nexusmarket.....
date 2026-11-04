package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identifier of a refund (UUID).
 */
public final class RefundId {

    private final UUID value;

    public RefundId(UUID value) {
        this.value = Objects.requireNonNull(value, "Refund id cannot be null");
    }

    public static RefundId of(UUID value) {
        return new RefundId(value);
    }

    public static RefundId of(String value) {
        return new RefundId(UUID.fromString(value));
    }

    public static RefundId random() {
        return new RefundId(UUID.randomUUID());
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
        RefundId refundId = (RefundId) o;
        return value.equals(refundId.value);
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
