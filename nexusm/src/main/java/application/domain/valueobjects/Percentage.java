package application.domain.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Percentage value object (discounts, taxes, etc.).
 *
 * <p>Validations:</p>
 * <ul>
 *   <li>Must be within the range [0, 100].</li>
 *   <li>Normalized to 2 decimal places with {@code HALF_UP} rounding.</li>
 * </ul>
 */
public final class Percentage {

    private final BigDecimal value;

    public Percentage(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100: " + value);
        }
        this.value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Percentage of(int value) {
        return new Percentage(BigDecimal.valueOf(value));
    }

    public static Percentage of(double value) {
        return new Percentage(BigDecimal.valueOf(value));
    }

    public static Percentage of(BigDecimal value) {
        return new Percentage(value);
    }

    /**
     * Percentage converted to a fraction, e.g. {@code 15} -> {@code 0.15}.
     */
    public BigDecimal asFraction() {
        return value.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
    }

    public BigDecimal getValue() {
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
        Percentage that = (Percentage) o;
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return value.stripTrailingZeros().toPlainString() + "%";
    }
}