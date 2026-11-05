package application.adapters.out.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The audit log only receives the events of committed transactions.
 */
class DomainEventAuditListenerTest {

    @Test
    void theAuditIsWrittenAfterTheCommit() {
        Method listener = java.util.Arrays.stream(DomainEventAuditListener.class.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(TransactionalEventListener.class))
            .findFirst()
            .orElseThrow();

        assertEquals(TransactionPhase.AFTER_COMMIT,
            listener.getAnnotation(TransactionalEventListener.class).phase());
    }
}
