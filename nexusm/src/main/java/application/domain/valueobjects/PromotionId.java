package application.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador único de una promoción.
 */
public final class PromotionId {

    private final UUID value;

    public PromotionId(UUID value) {
        this.value = Objects.requireNonNull(value, "El id de promoción no puede ser nulo");
    }

    public static PromotionId of(UUID value) {
        return new PromotionId(value);
    }

    public static PromotionId of(String value) {
        return new PromotionId(UUID.fromString(value));
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
        return value.equals(((PromotionId) o).value);
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
