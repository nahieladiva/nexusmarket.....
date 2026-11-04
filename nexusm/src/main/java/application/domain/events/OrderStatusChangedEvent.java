package application.domain.events;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.OrderId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando una orden cambia de estado.
 */
public record OrderStatusChangedEvent(OrderId orderId, OrderStatus previousStatus,
                                      OrderStatus newStatus,
                                      LocalDateTime occurredAt) implements DomainEvent {

    public OrderStatusChangedEvent {
        Objects.requireNonNull(orderId, "orderId es obligatorio");
        Objects.requireNonNull(previousStatus, "previousStatus es obligatorio");
        Objects.requireNonNull(newStatus, "newStatus es obligatorio");
        Objects.requireNonNull(occurredAt, "occurredAt es obligatorio");
    }
}