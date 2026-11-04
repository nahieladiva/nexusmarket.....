package application.domain.events;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando se crea una orden.
 */
public record OrderCreatedEvent(OrderId orderId, UserId buyerId, Money total,
                                LocalDateTime occurredAt) implements DomainEvent {

    public OrderCreatedEvent {
        Objects.requireNonNull(orderId, "orderId es obligatorio");
        Objects.requireNonNull(buyerId, "buyerId es obligatorio");
        Objects.requireNonNull(total, "total es obligatorio");
        Objects.requireNonNull(occurredAt, "occurredAt es obligatorio");
    }
}