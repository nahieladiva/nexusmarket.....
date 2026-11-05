package application.domain.services.order;

import application.domain.enums.OrderStatus;
import application.domain.enums.StockMovementType;
import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceStateConflictException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.services.inventory.RecordStockMovementService;
import application.domain.services.invoice.VoidInvoiceService;
import application.domain.services.refund.CreateRefundService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Cancels an order that has no shipments. The buyer who owns it or an administrator can do it, and
 * reserved stock goes back to its warehouse. If the order was paid, its invoice is voided and a
 * refund is created.
 */
@DomainService
public class CancelOrderService {

    private final OrderRepository orderRepository;
    private final OrderFulfillmentDomainService orderFulfillmentDomainService;
    private final RecordStockMovementService recordStockMovementService;
    private final NotifyOrderStatusChangeService notifyOrderStatusChangeService;
    private final VoidInvoiceService voidInvoiceService;
    private final ShipmentRepository shipmentRepository;
    private final CreateRefundService createRefundService;

    public CancelOrderService(OrderRepository orderRepository,
                              OrderFulfillmentDomainService orderFulfillmentDomainService,
                              RecordStockMovementService recordStockMovementService,
                              NotifyOrderStatusChangeService notifyOrderStatusChangeService,
                              VoidInvoiceService voidInvoiceService,
                              ShipmentRepository shipmentRepository,
                              CreateRefundService createRefundService) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.orderFulfillmentDomainService = Objects.requireNonNull(orderFulfillmentDomainService);
        this.recordStockMovementService = Objects.requireNonNull(recordStockMovementService);
        this.notifyOrderStatusChangeService = Objects.requireNonNull(notifyOrderStatusChangeService);
        this.voidInvoiceService = Objects.requireNonNull(voidInvoiceService);
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
        this.createRefundService = Objects.requireNonNull(createRefundService);
    }

    public Order execute(User actor, OrderId orderId) {
        actor.requireAnyRole(UserRole.BUYER, UserRole.ADMINISTRATOR);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        if (actor.isBuyer()) {
            order.requireBelongsTo(actor.getId());
        }
        if (!shipmentRepository.findByOrderId(orderId).isEmpty()) {
            throw new ResourceStateConflictException("Order " + orderId + " has shipments and cannot be cancelled");
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
        voidInvoiceService.execute(saved);
        if (saved.wasPaid()) {
            createRefundService.forCancelledOrder(saved);
        }
        notifyOrderStatusChangeService.execute(saved, previous);
        return saved;
    }
}
