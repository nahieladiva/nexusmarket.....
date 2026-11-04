package application.domain.enums;

import application.domain.exceptions.BusinessRuleException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Commercial status of a seller in the marketplace.
 *
 * <p>Allowed transitions:</p>
 * <ul>
 *   <li>{@code PENDING_APPROVAL} -> {@code APPROVED}</li>
 *   <li>{@code APPROVED} -> {@code SUSPENDED}</li>
 *   <li>{@code SUSPENDED} -> {@code APPROVED}</li>
 * </ul>
 */
public enum SellerStatus {

    PENDING_APPROVAL,
    APPROVED,
    SUSPENDED;

    private static final Map<SellerStatus, Set<SellerStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<SellerStatus, Set<SellerStatus>> transitions = new EnumMap<>(SellerStatus.class);
        transitions.put(PENDING_APPROVAL, Set.of(APPROVED));
        transitions.put(APPROVED, Set.of(SUSPENDED));
        transitions.put(SUSPENDED, Set.of(APPROVED));
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    /**
     * Tells whether the seller can move to the target status.
     */
    public boolean canTransitionTo(SellerStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Validates the transition and throws {@link BusinessRuleException} when it is not allowed.
     */
    public void requireCanTransitionTo(SellerStatus target) {
        if (!canTransitionTo(target)) {
            throw new BusinessRuleException(
                    "Seller status cannot change from " + this + " to " + target);
        }
    }

    /**
     * Only approved sellers can publish products and receive orders.
     */
    public boolean canPublishProducts() {
        return this == APPROVED;
    }
}
