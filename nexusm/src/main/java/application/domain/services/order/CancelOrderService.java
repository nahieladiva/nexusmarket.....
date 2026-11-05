package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.enums.StockMovementType;
import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.ports.out.OrderRepository;
import application.domain.services.DomainService;
import application.domain.services.inventory.RecordStockMovementService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Cancels an order. The buyer who owns it or an administrator can do it, and reserved stock goes
 * back to its warehouse.
 */
@DomainService
public class CancelOrderService {

    private final OrderRepository orderRepository;
    private final OrderFulfillmentDomainService orderFulfillmentDomainService;
    private final RecordStockMovementService recordStockMovementService;
    private final NotifyOrderStatusChangeService notifyOrderStatusChangeService;

    public CancelOrderService(OrderRepository orderRepository,
                              OrderFulfillmentDomainService orderFulfillmentDomainService,
                              RecordStockMovementService recordStockMovementService,
                              NotifyOrderStatusChangeService notifyOrderStatusChangeService) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.orderFulfillmentDomainService = Objects.requireNonNull(orderFulfillmentDomainService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
        this.notifyOrderStatusChangeService = Objects.requireNonNull(notifyOrderStatusChangeService);
    }

    public Order execute(User actor, OrderId orderId) {
        actor.requireAnyRole(UserRole.BUYER, UserRole.ADMINISTRATOR);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        if (actor.isBuyer()) {
            order.requireBelongsTo(actor.getId());
        }
        OrderStatus previous = order.getStatus();
        List<Inventory> released = orderFulfillmentDomainService.cancel(order);
        Order saved = orderRepository.save(order);
        List<OrderItem> shippable = saved.getItems().stream()
            .filter(OrderItem::requiresShipment).toList();
        for (int i = 0; i < released.size(); i++) {
            recordStockMovementService.execute(released.get(i), StockMovementType.RELEASE,
                shippable.get(i).getQuantity().getValue(), "Released by cancelled order",
                actor.getId(), saved.getId().toString());
        }
        notifyOrderStatusChangeService.execute(saved, previous);
        return saved;
    }
}
