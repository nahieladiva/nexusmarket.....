package application.domain.ports.out;

import application.domain.events.DomainEvent;

/**
 * Puerto de salida 6/15 — bitácora de eventos de dominio (MongoDB).
 */
public interface AuditLogPort {

    void record(DomainEvent event);
}