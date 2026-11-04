package application.domain.enums;

/**
 * Ownership type of a warehouse.
 *
 * <ul>
 *   <li>{@code MARKETPLACE}: operated by NexusMarket and managed by administrators.</li>
 *   <li>{@code SELLER}: owned by a specific seller, who manages it.</li>
 * </ul>
 */
public enum WarehouseType {

    MARKETPLACE,
    SELLER;

    /**
     * Tells whether the warehouse must be linked to an owner seller.
     */
    public boolean requiresOwnerSeller() {
        return this == SELLER;
    }
}
