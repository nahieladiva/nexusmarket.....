package application.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Phone number value object.
 *
 * <p>Validations:</p>
 * <ul>
 *   <li>Must be in E.164 international format (e.g. {@code +521234567890}).</li>
 *   <li>Spaces and hyphens are normalized automatically.</li>
 * </ul>
 */
public final class PhoneNumber {

    // E.164: + followed by 1 to 15 digits.
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private final String value;

    public PhoneNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }
        String normalized = value.trim().replaceAll("[\\s-]", "");
        if (!E164_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Phone number must be in E.164 international format (e.g. +521234567890): " + value);
        }
        this.value = normalized;
    }

    public static PhoneNumber of(String value) {
        return new PhoneNumber(value);
    }

    public String getValue() {
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
        PhoneNumber that = (PhoneNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}