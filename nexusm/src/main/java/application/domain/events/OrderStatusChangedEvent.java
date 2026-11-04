package application.domain.events;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.OrderId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event raised when an order changes status.
 */
public record OrderStatusChangedEvent(OrderId orderId, OrderStatus previousStatus,
                                      OrderStatus newStatus,
                                      LocalDateTime occurredAt) implements DomainEvent {

    public OrderStatusChangedEvent {
        Objects.requireNonNull(orderId, "orderId is required");
        Objects.requireNonNull(previousStatus, "previousStatus is required");
        Objects.requireNonNull(newStatus, "newStatus is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }
}