package application.domain.enums;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceStateConflictException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Status of a return requested by a buyer for a delivered order.
 *
 * <p>Allowed transitions:</p>
 * <ul>
 *   <li>{@code REQUESTED} -> {@code APPROVED}, {@code REJECTED}</li>
 *   <li>{@code APPROVED} -> {@code RECEIVED}</li>
 *   <li>{@code REJECTED}, {@code RECEIVED} -> terminal statuses</li>
 * </ul>
 */
public enum ReturnStatus {

    REQUESTED,
    APPROVED,
    REJECTED,
    RECEIVED;

    private static final Map<ReturnStatus, Set<ReturnStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ReturnStatus, Set<ReturnStatus>> transitions = new EnumMap<>(ReturnStatus.class);
        transitions.put(REQUESTED, Set.of(APPROVED, REJECTED));
        transitions.put(APPROVED, Set.of(RECEIVED));
        transitions.put(REJECTED, Set.of());
        transitions.put(RECEIVED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    /**
     * Tells whether the return can move to the target status.
     */
    public boolean canTransitionTo(ReturnStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Validates the transition and throws {@link BusinessRuleException} when it is not allowed.
     */
    public void requireCanTransitionTo(ReturnStatus target) {
        if (!canTransitionTo(target)) {
            throw new ResourceStateConflictException(
                    "Return status cannot change from " + this + " to " + target);
        }
    }

    /**
     * A refund is created when the returned products are received.
     */
    public boolean entitlesRefund() {
        return this == RECEIVED;
    }

    /**
     * While a return is open, the buyer cannot request another one for the same order.
     */
    public boolean isOpen() {
        return this == REQUESTED || this == APPROVED;
    }

    /**
     * Every return that was not rejected counts against the units bought.
     */
    public boolean countsAsReturned() {
        return this != REJECTED;
    }
}
