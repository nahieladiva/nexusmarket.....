package application.domain.enums;

import application.domain.exceptions.OrderStateTransitionException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Estados por los que transita una orden de compra.
 *
 * <p>Transiciones permitidas:</p>
 * <ul>
 *   <li>{@code PENDING} -> {@code CONFIRMED}, {@code CANCELLED}</li>
 *   <li>{@code CONFIRMED} -> {@code SHIPPED}, {@code CANCELLED}</li>
 *   <li>{@code SHIPPED} -> {@code DELIVERED}</li>
 *   <li>{@code DELIVERED}, {@code CANCELLED} -> estados terminales</li>
 * </ul>
 */
public enum OrderStatus {

    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<OrderStatus, Set<OrderStatus>> transitions = new EnumMap<>(OrderStatus.class);
        transitions.put(PENDING, Set.of(CONFIRMED, CANCELLED));
        transitions.put(CONFIRMED, Set.of(SHIPPED, CANCELLED));
        transitions.put(SHIPPED, Set.of(DELIVERED));
        transitions.put(DELIVERED, Set.of());
        transitions.put(CANCELLED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    /**
     * Indica si la orden puede transicionar al estado objetivo.
     */
    public boolean canTransitionTo(OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Valida la transición y lanza {@link OrderStateTransitionException}
     * si no está permitida.
     */
    public void requireCanTransitionTo(OrderStatus target) {
        if (!canTransitionTo(target)) {
            throw new OrderStateTransitionException(null, this, target);
        }
    }

    /**
     * Útil para deserializar valores enviados desde la capa de transporte.
     */
    public static OrderStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El estado de la orden es obligatorio");
        }
        try {
            return OrderStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Estado de orden inválido: " + value);
        }
    }
}