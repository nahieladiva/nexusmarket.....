package application.domain.valueobjects;

import java.util.Objects;

/**
 * Quantity value object (inventory, units in an order).
 *
 * <p>Validations:</p>
 * <ul>
 *   <li>Must be a whole number.</li>
 *   <li>Cannot be negative.</li>
 *   <li>Arithmetic operations can never produce negative values.</li>
 * </ul>
 */
public final class Quantity implements Comparable<Quantity> {

    private final int value;

    public Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + value);
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        return new Quantity(value);
    }

    public static Quantity zero() {
        return new Quantity(0);
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "Quantity to add cannot be null");
        return new Quantity(this.value + other.value);
    }

    public Quantity subtract(Quantity other) {
        Objects.requireNonNull(other, "Quantity to subtract cannot be null");
        return new Quantity(this.value - other.value);
    }

    public int getValue() {
        return value;
    }

    public boolean isZero() {
        return value == 0;
    }

    @Override
    public int compareTo(Quantity other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quantity quantity = (Quantity) o;
        return value == quantity.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}