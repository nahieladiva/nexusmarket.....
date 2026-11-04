package application.domain.ports.out;

import application.domain.events.DomainEvent;

/**
 * Puerto de salida 12/15 — publica eventos de dominio para quien los quiera escuchar
 * (por ejemplo, la bitácora de auditoría).
 */
public interface DomainEventPublisherPort {

    void publish(DomainEvent event);
}
