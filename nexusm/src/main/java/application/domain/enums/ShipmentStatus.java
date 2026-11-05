package application.domain.enums;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceStateConflictException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Status of the shipment of a paid order, handled by the logistics operator.
 *
 * <p>Allowed transitions:</p>
 * <ul>
 *   <li>{@code PREPARING} -> {@code PACKED}</li>
 *   <li>{@code PACKED} -> {@code IN_TRANSIT}</li>
 *   <li>{@code IN_TRANSIT} -> {@code DELIVERED}</li>
 *   <li>{@code DELIVERED} -> terminal status</li>
 * </ul>
 */
public enum ShipmentStatus {

    PREPARING,
    PACKED,
    IN_TRANSIT,
    DELIVERED;

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ShipmentStatus, Set<ShipmentStatus>> transitions = new EnumMap<>(ShipmentStatus.class);
        transitions.put(PREPARING, Set.of(PACKED));
        transitions.put(PACKED, Set.of(IN_TRANSIT));
        transitions.put(IN_TRANSIT, Set.of(DELIVERED));
        transitions.put(DELIVERED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    /**
     * Tells whether the shipment can move to the target status.
     */
    public boolean canTransitionTo(ShipmentStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Validates the transition and throws {@link BusinessRuleException} when it is not allowed.
     */
    public void requireCanTransitionTo(ShipmentStatus target) {
        if (!canTransitionTo(target)) {
            throw new ResourceStateConflictException(
                    "Shipment status cannot change from " + this + " to " + target);
        }
    }

    /**
     * Once the shipment leaves the warehouse, the order is considered shipped.
     */
    public boolean hasLeftWarehouse() {
        return this == IN_TRANSIT || this == DELIVERED;
    }

    public boolean isTerminal() {
        return this == DELIVERED;
    }
}
