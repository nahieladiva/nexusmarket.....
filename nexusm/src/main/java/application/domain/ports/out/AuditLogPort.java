package application.domain.ports.out;

import application.domain.events.DomainEvent;

/**
 * Output port 6/15 — domain event log (MongoDB).
 */
public interface AuditLogPort {

    void record(DomainEvent event);
}