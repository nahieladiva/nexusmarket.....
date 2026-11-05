package application.adapters.out.events;

import application.domain.events.DomainEvent;
import application.domain.ports.out.AuditLogPort;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Saves each domain event in the audit log (MongoDB), only after the MySQL transaction
 * commits, so changes that were rolled back are not audited.
 */
@Component
public class DomainEventAuditListener {

    private final AuditLogPort auditLogPort;

    public DomainEventAuditListener(AuditLogPort auditLogPort) {
        this.auditLogPort = Objects.requireNonNull(auditLogPort, "auditLogPort is required");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(DomainEvent event) {
        auditLogPort.record(event);
    }
}
