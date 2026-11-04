package application.domain.exceptions;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.OrderId;

/**
 * Thrown when an order is asked to make a status transition that is not allowed.
 */
public class OrderStateTransitionException extends DomainException {

    private final OrderId orderId;
    private final OrderStatus current;
    private final OrderStatus target;

    public OrderStateTransitionException(OrderId orderId, OrderStatus current, OrderStatus target) {
        super("Order " + (orderId == null ? "" : orderId + " ")
            + "cannot change status from " + current + " to " + target);
        this.orderId = orderId;
        this.current = current;
        this.target = target;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public OrderStatus getCurrent() {
        return current;
    }

    public OrderStatus getTarget() {
        return target;
    }
}
