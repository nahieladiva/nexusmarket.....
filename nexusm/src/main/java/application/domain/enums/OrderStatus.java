package application.domain.enums;

import application.domain.exceptions.OrderStateTransitionException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Statuses of a purchase order.
 *
 * <p>Allowed transitions:</p>
 * <ul>
 *   <li>{@code PENDING_PAYMENT} -> {@code PAID}, {@code CANCELLED}</li>
 *   <li>{@code PAID} -> {@code SHIPPED}, {@code CANCELLED}</li>
 *   <li>{@code SHIPPED} -> {@code DELIVERED}</li>
 *   <li>{@code DELIVERED}, {@code CANCELLED} -> terminal statuses</li>
 * </ul>
 */
public enum OrderStatus {

    PENDING_PAYMENT,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(PENDING_PAYMENT, Set.of(PAID, CANCELLED));
        transitions.put(PAID, Set.of(SHIPPED, CANCELLED));
        transitions.put(SHIPPED, Set.of(DELIVERED));
        transitions.put(DELIVERED, Set.of());
        transitions.put(CANCELLED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    /**
     * Tells whether the order can move to the target status.
     */
    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Validates the transition and throws {@link OrderStateTransitionException} when it is not allowed.
     */
    public void requireCanTransitionTo(OrderStatus target) {
        if (!canTransitionTo(target)) {
            throw new OrderStateTransitionException(null, this, target);
        }
    }

    /**
     * An order can be cancelled while it has not been shipped.
     */
    public boolean isCancellable() {
        return canTransitionTo(CANCELLED);
    }

    /**
     * A paid order that has not been shipped keeps stock reserved in a warehouse.
     */
    public boolean keepsStockReserved() {
        return this == PAID;
    }

    /**
     * Only delivered orders can have returns requested.
     */
    public boolean allowsReturns() {
        return this == DELIVERED;
    }

    public boolean isTerminal() {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).isEmpty();
    }

    /**
     * Converts a text value (for example, from a request) into an order status.
     *
     * @throws IllegalArgumentException when the value is blank or is not a valid status
     */
    public static OrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order status is required");
        }
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid order status: " + value);
        }
    }
}
