package application.domain.events;

import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event raised when a seller adds a product.
 */
public record ProductAddedEvent(ProductId productId, ProductCode code, UserId sellerId,
                                LocalDateTime occurredAt) implements DomainEvent {

    public ProductAddedEvent {
        Objects.requireNonNull(productId, "productId is required");
        Objects.requireNonNull(code, "code is required");
        Objects.requireNonNull(sellerId, "sellerId is required");
        Objects.requireNonNull(occurredAt, "occurredAt is required");
    }
}