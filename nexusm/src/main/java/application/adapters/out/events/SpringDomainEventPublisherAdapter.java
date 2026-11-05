package application.adapters.out.events;

import application.domain.events.DomainEvent;
import application.domain.ports.out.DomainEventPublisherPort;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter of {@link DomainEventPublisherPort} on Spring events.
 */
@Component
public class SpringDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private final ApplicationEventPublisher publisher;

    public SpringDomainEventPublisherAdapter(ApplicationEventPublisher publisher) {
        this.publisher = Objects.requireNonNull(publisher, "publisher is required");
    }

    @Override
    public void publish(DomainEvent event) {
        publisher.publishEvent(event);
    }
}
