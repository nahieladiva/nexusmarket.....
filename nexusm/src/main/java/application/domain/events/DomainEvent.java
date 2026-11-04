package application.domain.events;

import java.time.LocalDateTime;

/**
 * Marker for domain events. Every event occurs at a given instant.
 */
public interface DomainEvent {

    LocalDateTime occurredAt();
}