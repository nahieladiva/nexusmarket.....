package application.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Product code (SKU) value object.
 *
 * <p>Format: 2 to 5 uppercase letters, a hyphen and 4 to 8 digits, e.g. {@code ELE-000123}.</p>
 */
public final class ProductCode {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z]{2,5}-\\d{4,8}$");
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^[A-Z]{2,5}$");

    private final String value;

    public ProductCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Product code (SKU) cannot be null or blank");
        }
        String normalized = value.trim().toUpperCase();
        if (!SKU_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Invalid SKU format: " + value + ". Expected format: XXX-0000");
        }
        this.value = normalized;
    }

    public static ProductCode of(String value) {
        return new ProductCode(value);
    }

    /**
     * Generation rule: category code + hyphen + sequence.
     *
     * @param categoryCode 2 to 5 letters, e.g. {@code ELEC}
     * @param sequence     sequence number
     */
    public static ProductCode generate(String categoryCode, int sequence) {
        if (categoryCode == null || !CATEGORY_PATTERN.matcher(categoryCode.toUpperCase()).matches()) {
            throw new IllegalArgumentException(
                "Invalid category code: " + categoryCode + ". Expected format: 2-5 letters");
        }
        if (sequence < 0) {
            throw new IllegalArgumentException("Sequence cannot be negative");
        }
        return new ProductCode(String.format("%s-%04d", categoryCode.toUpperCase(), sequence));
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
        ProductCode that = (ProductCode) o;
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