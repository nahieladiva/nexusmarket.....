package application.adapters.out.persistence.mongodb.mappers;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.domain.events.DomainEvent;
import application.domain.events.InventoryUpdatedEvent;
import application.domain.events.LowStockEvent;
import application.domain.events.OrderCreatedEvent;
import application.domain.events.OrderStatusChangedEvent;
import application.domain.events.ProductAddedEvent;
import org.springframework.stereotype.Component;

/**
 * Convierte eventos de dominio en documentos de la bitácora de auditoría.
 */
@Component
public class AuditLogMapper {

    public AuditLogDocument toDocument(DomainEvent event) {
        return new AuditLogDocument(
            event.getClass().getSimpleName(),
            extractAggregateId(event),
            serialize(event),
            event.occurredAt());
    }

    private String extractAggregateId(DomainEvent event) {
        if (event instanceof OrderCreatedEvent created) {
            return created.orderId().toString();
        }
        if (event instanceof OrderStatusChangedEvent changed) {
            return changed.orderId().toString();
        }
        if (event instanceof InventoryUpdatedEvent updated) {
            return updated.productId() + "|" + updated.warehouseId();
        }
        if (event instanceof ProductAddedEvent added) {
            return added.productId().toString();
        }
        if (event instanceof LowStockEvent lowStock) {
            return lowStock.productId() + "|" + lowStock.warehouseId();
        }
        return event.getClass().getSimpleName();
    }

    /**
     * Los eventos son records: su toString() ya lista todos los campos con su valor.
     * (Se evita depender de Jackson, que cambió de versión en Spring Boot 4.)
     */
    private String serialize(DomainEvent event) {
        return event.toString();
    }
}
