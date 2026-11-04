package application.adapters.out.events;

import application.domain.events.DomainEvent;
import application.domain.ports.out.AuditLogPort;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Guarda en la bitácora (MongoDB) cada evento de dominio, solo cuando la transacción
 * de MySQL se confirmó; así no quedan auditados cambios que se revirtieron.
 */
@Component
public class DomainEventAuditListener {

    private final AuditLogPort auditLogPort;

    public DomainEventAuditListener(AuditLogPort auditLogPort) {
        this.auditLogPort = Objects.requireNonNull(auditLogPort, "auditLogPort es obligatorio");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(DomainEvent event) {
        auditLogPort.record(event);
    }
}
