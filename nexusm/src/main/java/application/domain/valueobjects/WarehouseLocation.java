package application.domain.valueobjects;

import java.util.Locale;
import java.util.Objects;

/**
 * Value object for a physical location inside a warehouse (aisle, shelf and bin).
 */
public final class WarehouseLocation {

    private final String aisle;
    private final String shelf;
    private final String bin;

    public WarehouseLocation(String aisle, String shelf, String bin) {
        this.aisle = requireNotBlank(aisle, "Aisle is required");
        this.shelf = requireNotBlank(shelf, "Shelf is required");
        this.bin = requireNotBlank(bin, "Bin is required");
    }

    public static WarehouseLocation of(String aisle, String shelf, String bin) {
        return new WarehouseLocation(aisle, shelf, bin);
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "-");
    }

    public String getAisle() {
        return aisle;
    }

    public String getShelf() {
        return shelf;
    }

    public String getBin() {
        return bin;
    }

    public String code() {
        return aisle + "/" + shelf + "/" + bin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WarehouseLocation that = (WarehouseLocation) o;
        return aisle.equals(that.aisle)
            && shelf.equals(that.shelf)
            && bin.equals(that.bin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aisle, shelf, bin);
    }

    @Override
    public String toString() {
        return code();
    }
}