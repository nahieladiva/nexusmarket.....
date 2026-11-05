package application.domain.services.shipment;

import application.domain.enums.OrderStatus;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Order;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.services.order.NotifyOrderStatusChangeService;
import application.domain.valueobjects.OrderId;

import java.util.Objects;

/**
 * Moves an order forward according to its shipments and notifies the buyer when its status changes.
 */
@DomainService
public class ApplyShipmentProgressService {

    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final NotifyOrderStatusChangeService notifyOrderStatusChangeService;

    public ApplyShipmentProgressService(OrderRepository orderRepository,
                                        ShipmentRepository shipmentRepository,
                                        NotifyOrderStatusChangeService notifyOrderStatusChangeService) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
        this.notifyOrderStatusChangeService = Objects.requireNonNull(notifyOrderStatusChangeService);
    }

    public void execute(OrderId orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        OrderStatus previous = order.getStatus();
        order.applyShipmentProgress(shipmentRepository.findByOrderId(orderId));
        if (order.getStatus() != previous) {
            notifyOrderStatusChangeService.execute(orderRepository.save(order), previous);
        }
    }
}
