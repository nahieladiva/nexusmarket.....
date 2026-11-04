package application.domain.enums;

/**
 * Types of inventory movement recorded in the stock history.
 */
public enum StockMovementType {

    /** Initial stock of a product in a warehouse. */
    INITIAL,
    /** Inbound goods (restocking). */
    RESTOCK,
    /** Manual adjustment (physical count, shrinkage, damage). It can be positive or negative. */
    ADJUSTMENT,
    /** Outbound reservation when an order is paid. */
    RESERVATION,
    /** Stock returned to the warehouse when a paid order is cancelled. */
    RELEASE,
    /** Stock returned to the warehouse when a buyer's return is received. */
    RETURN;

    /**
     * Tells whether the movement adds units to the warehouse.
     */
    public boolean isInbound() {
        return this == INITIAL || this == RESTOCK || this == RELEASE || this == RETURN;
    }

    /**
     * Only manual adjustments can have either sign; the rest have a fixed direction.
     */
    public boolean allowsAnySign() {
        return this == ADJUSTMENT;
    }
}
