package application.support;

import application.domain.events.DomainEvent;
import application.domain.ports.out.DomainEventPublisherPort;
import java.util.ArrayList;
import java.util.List;

public class RecordingEventPublisher implements DomainEventPublisherPort {
    public final List<DomainEvent> events = new ArrayList<>();

    @Override public void publish(DomainEvent event) { events.add(event); }

    public long count(Class<? extends DomainEvent> type) {
        return events.stream().filter(type::isInstance).count();
    }
}
