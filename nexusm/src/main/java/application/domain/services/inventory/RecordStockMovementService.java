package application.domain.services.inventory;

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
import application.domain.services.DomainService;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Applies the rules that follow every stock change: the movement is stored in the history,
 * {@link InventoryUpdatedEvent} is published and, when stock falls below the reorder threshold,
 * {@link LowStockEvent} is published and the seller is notified.
 */
@DomainService
public class RecordStockMovementService {

    private final StockMovementLogPort movementLog;
    private final DomainEventPublisherPort eventPublisher;
    private final NotificationPort notificationPort;
    private final ProductRepository productRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public RecordStockMovementService(StockMovementLogPort movementLog,
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

    /**
     * @param performedBy user who made the change; {@code null} when the system made it
     * @param reference   optional reference, such as the order id
     */
    public void execute(Inventory inventory, StockMovementType type, int delta, String reason,
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
                notificationPort.notify(product.getSellerId(), "Low stock",
                    "Product " + product.getName() + " (" + product.getCode() + ") has "
                        + inventory.getOnHand() + " units left in warehouse "
                        + inventory.getWarehouseId() + "; the reorder threshold is "
                        + inventory.getReorderThreshold() + "."));
        }
    }
}
