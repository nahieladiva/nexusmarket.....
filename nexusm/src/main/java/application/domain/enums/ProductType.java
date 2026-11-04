package application.domain.enums;

/**
 * Type of product sold in the marketplace.
 *
 * <ul>
 *   <li>{@code PHYSICAL}: has inventory in warehouses and requires a shipment.</li>
 *   <li>{@code DIGITAL}: has no inventory and is delivered without a shipment.</li>
 * </ul>
 */
public enum ProductType {

    PHYSICAL,
    DIGITAL;

    /**
     * Tells whether the product needs stock in a warehouse.
     */
    public boolean requiresInventory() {
        return this == PHYSICAL;
    }

    /**
     * Tells whether the product must be delivered through a shipment.
     */
    public boolean requiresShipment() {
        return this == PHYSICAL;
    }

    /**
     * Tells whether the product can be returned. Digital products cannot.
     */
    public boolean isReturnable() {
        return this == PHYSICAL;
    }
}
