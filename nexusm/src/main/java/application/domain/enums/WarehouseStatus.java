package application.domain.enums;

import application.domain.exceptions.BusinessRuleException;

/**
 * Operational status of a warehouse. Only active warehouses can receive
 * stock and take part in order allocation.
 */
public enum WarehouseStatus {

    ACTIVE,
    INACTIVE;

    /**
     * Tells whether the warehouse can store and ship inventory.
     */
    public boolean isOperational() {
        return this == ACTIVE;
    }

    /**
     * Validates the transition and throws {@link BusinessRuleException} when
     * the warehouse is already in the target status.
     */
    public void requireCanTransitionTo(WarehouseStatus target) {
        if (this == target) {
            throw new BusinessRuleException("Warehouse is already " + target);
        }
    }
}
