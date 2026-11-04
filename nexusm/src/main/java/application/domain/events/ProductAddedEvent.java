package application.domain.events;

import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Evento emitido cuando un vendedor publica un producto.
 */
public record ProductAddedEvent(ProductId productId, ProductCode code, UserId sellerId,
                                LocalDateTime occurredAt) implements DomainEvent {

    public ProductAddedEvent {
        Objects.requireNonNull(productId, "productId es obligatorio");
        Objects.requireNonNull(code, "code es obligatorio");
        Objects.requireNonNull(sellerId, "sellerId es obligatorio");
        Objects.requireNonNull(occurredAt, "occurredAt es obligatorio");
    }
}