package application.domain.enums;

import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.ResourceStateConflictException;

/**
 * Status of a refund to the buyer, processed by an administrator.
 *
 * <p>Allowed transitions: {@code PENDING} -> {@code PROCESSED} or {@code REJECTED}.
 * Both are terminal statuses.</p>
 */
public enum RefundStatus {

    PENDING,
    PROCESSED,
    REJECTED;

    /**
     * Tells whether the refund can move to the target status.
     */
    public boolean canTransitionTo(RefundStatus target) {
        return this == PENDING && (target == PROCESSED || target == REJECTED);
    }

    /**
     * Validates the transition and throws {@link BusinessRuleException} when it is not allowed.
     */
    public void requireCanTransitionTo(RefundStatus target) {
        if (!canTransitionTo(target)) {
            throw new ResourceStateConflictException(
                    "Refund status cannot change from " + this + " to " + target);
        }
    }

    public boolean isTerminal() {
        return this != PENDING;
    }
}
