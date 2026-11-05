package application.domain.enums;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceStateConflictException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Lifecycle status of a product in the catalog.
 *
 * <p>Allowed transitions:</p>
 * <ul>
 *   <li>{@code DRAFT} -> {@code PUBLISHED}, {@code DISCONTINUED}</li>
 *   <li>{@code PUBLISHED} -> {@code SUSPENDED}, {@code BLOCKED}, {@code DISCONTINUED}</li>
 *   <li>{@code SUSPENDED} -> {@code PUBLISHED}, {@code BLOCKED}, {@code DISCONTINUED}</li>
 *   <li>{@code BLOCKED} -> {@code SUSPENDED}, only when an administrator unblocks it</li>
 *   <li>{@code DISCONTINUED} -> terminal status</li>
 * </ul>
 */
public enum ProductStatus {

    DRAFT,
    PUBLISHED,
    SUSPENDED,
    BLOCKED,
    DISCONTINUED;

    private static final Map<ProductStatus, Set<ProductStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ProductStatus, Set<ProductStatus>> transitions = new EnumMap<>(ProductStatus.class);
        transitions.put(DRAFT, Set.of(PUBLISHED, DISCONTINUED));
        transitions.put(PUBLISHED, Set.of(SUSPENDED, BLOCKED, DISCONTINUED));
        transitions.put(SUSPENDED, Set.of(PUBLISHED, BLOCKED, DISCONTINUED));
        transitions.put(BLOCKED, Set.of(SUSPENDED));
        transitions.put(DISCONTINUED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    /**
     * Tells whether the product can move to the target status.
     */
    public boolean canTransitionTo(ProductStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Validates the transition and throws {@link BusinessRuleException} when it is not allowed.
     */
    public void requireCanTransitionTo(ProductStatus target) {
        if (!canTransitionTo(target)) {
            throw new ResourceStateConflictException(
                    "Product status cannot change from " + this + " to " + target);
        }
    }

    /**
     * Only published products are visible in the public catalog and can be purchased.
     */
    public boolean isSellable() {
        return this == PUBLISHED;
    }

    /**
     * A discontinued product can no longer be edited or published.
     */
    public boolean isTerminal() {
        return this == DISCONTINUED;
    }
}
