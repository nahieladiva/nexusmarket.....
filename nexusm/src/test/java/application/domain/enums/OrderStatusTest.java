package application.domain.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests of the {@link OrderStatus} state machine.
 */
class OrderStatusTest {

    @Test
    void allowsPendingPaymentTransitions() {
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.PAID));
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void allowsPaidTransitions() {
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.DELIVERED));
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void allowsShippedToDelivered() {
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void forbidsInvalidTransitions() {
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.DELIVERED));
        assertFalse(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.PENDING_PAYMENT));
    }

    @Test
    void onlyDeliveredOrdersAllowReturns() {
        assertTrue(OrderStatus.DELIVERED.allowsReturns());
        assertFalse(OrderStatus.SHIPPED.allowsReturns());
    }

    @Test
    void deliveredAndCancelledAreTerminal() {
        assertTrue(OrderStatus.DELIVERED.isTerminal());
        assertTrue(OrderStatus.CANCELLED.isTerminal());
        assertFalse(OrderStatus.PAID.isTerminal());
    }
}
