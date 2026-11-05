package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.enums.StockMovementType;
import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.OrderRepository;
import application.domain.services.DomainService;
import application.domain.services.inventory.RecordStockMovementService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Registers the payment of an order: the buyer who owns it pays, its stock is reserved and, when
 * no item requires shipment, the order is delivered right away.
 */
@DomainService
public class PayOrderService {

    private final OrderRepository orderRepository;
    private final OrderFulfillmentDomainService orderFulfillmentDomainService;
    private final RecordStockMovementService recordStockMovementService;
    private final NotifyOrderStatusChangeService notifyOrderStatusChangeService;
    private final ClockPort clock;

    public PayOrderService(OrderRepository orderRepository,
                           OrderFulfillmentDomainService orderFulfillmentDomainService,
                           RecordStockMovementService recordStockMovementService,
                           NotifyOrderStatusChangeService notifyOrderStatusChangeService,
                           ClockPort clock) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.orderFulfillmentDomainService = Objects.requireNonNull(orderFulfillmentDomainService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
        this.notifyOrderStatusChangeService = Objects.requireNonNull(notifyOrderStatusChangeService);
        this.clock = Objects.requireNonNull(clock);
    }

    public Order execute(User buyer, OrderId orderId) {
        buyer.requireRole(UserRole.BUYER);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        order.requireBelongsTo(buyer.getId());
        OrderStatus previous = order.getStatus();
        List<Inventory> reserved = orderFulfillmentDomainService.confirm(order, clock.now());
        Order saved = orderRepository.save(order);
        List<OrderItem> shippable = saved.getItems().stream()
            .filter(OrderItem::requiresShipment).toList();
        for (int i = 0; i < reserved.size(); i++) {
            recordStockMovementService.execute(reserved.get(i), StockMovementType.RESERVATION,
                -shippable.get(i).getQuantity().getValue(), "Reserved for paid order",
                buyer.getId(), saved.getId().toString());
        }
        notifyOrderStatusChangeService.execute(saved, previous);
        return saved;
    }
}
