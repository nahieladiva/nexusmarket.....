package application.domain.ports.out;

import application.domain.events.DomainEvent;

/**
 * Output port — publishes domain events to anyone who wants to listen
 * (for example, the audit log).
 */
public interface DomainEventPublisherPort {

    void publish(DomainEvent event);
}
