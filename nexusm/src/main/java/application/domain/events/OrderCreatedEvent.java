package application.domain.events;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event raised when an order is created.
 */
public record OrderCreatedEvent(OrderId orderId, UserId buyerId, Money total,
                                LocalDateTime occurredAt) implements DomainEvent {

    public OrderCreatedEvent {
        Objects.requireNonNull(orderId, "orderId is required");
        Objects.requireNonNull(buyerId, "buyerId is required");
        Objects.requireNonNull(total, "total is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }
}