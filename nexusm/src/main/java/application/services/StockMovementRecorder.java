package application.services;

import application.domain.enums.StockMovementType;
import application.domain.events.InventoryUpdatedEvent;
import application.domain.events.LowStockEvent;
import application.domain.models.Inventory;
import application.domain.models.StockMovement;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.DomainEventPublisherPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.NotificationPort;
import application.domain.ports.out.ProductRepository;
import application.domain.ports.out.StockMovementLogPort;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Aplica las reglas que acompañan a todo cambio de stock:
 * <ul>
 *   <li>cada movimiento queda en el historial con su motivo;</li>
 *   <li>se publica {@link InventoryUpdatedEvent};</li>
 *   <li>si el stock queda por debajo del umbral, se publica {@link LowStockEvent}
 *       y se avisa al vendedor dueño del producto.</li>
 * </ul>
 */
@Component
public class StockMovementRecorder {

    private final StockMovementLogPort movementLog;
    private final DomainEventPublisherPort eventPublisher;
    private final NotificationPort notificationPort;
    private final ProductRepository productRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public StockMovementRecorder(StockMovementLogPort movementLog,
                                 DomainEventPublisherPort eventPublisher,
                                 NotificationPort notificationPort,
                                 ProductRepository productRepository,
                                 ClockPort clock, IdGeneratorPort idGenerator) {
        this.movementLog = Objects.requireNonNull(movementLog);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public void record(Inventory inventory, StockMovementType type, int delta, String reason,
                       UserId performedBy, String reference) {
        LocalDateTime now = clock.now();
        movementLog.record(new StockMovement(idGenerator.newId().toString(),
            inventory.getProductId(), inventory.getWarehouseId(), type, delta,
            inventory.getOnHand(), reason, performedBy, reference, now));
        eventPublisher.publish(new InventoryUpdatedEvent(
            inventory.getProductId(), inventory.getWarehouseId(), inventory.getOnHand(), now));
        if (delta < 0 && inventory.isBelowReorderPoint()) {
            eventPublisher.publish(new LowStockEvent(inventory.getProductId(),
                inventory.getWarehouseId(), inventory.getOnHand(),
                inventory.getReorderThreshold(), now));
            productRepository.findById(inventory.getProductId()).ifPresent(product ->
                notificationPort.notify(product.getSellerId(), "Stock bajo",
                    "El producto " + product.getName() + " (" + product.getCode()
                        + ") quedó con " + inventory.getOnHand()
                        + " unidades en la bodega " + inventory.getWarehouseId()
                        + "; el umbral de reorden es " + inventory.getReorderThreshold() + "."));
        }
    }
}
