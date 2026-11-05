package application.domain.services.shipment;

import application.domain.enums.UserRole;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Buyer;
import application.domain.models.Order;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.IdGeneratorPort;
import application.domain.ports.out.OrderRepository;
import application.domain.ports.out.ShipmentRepository;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Creates the shipment of a paid order from one of its warehouses. Each warehouse of the order has
 * only one shipment, addressed to the default shipping address of the buyer.
 */
@DomainService
public class CreateShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ClockPort clock;
    private final IdGeneratorPort idGenerator;

    public CreateShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
                                 UserRepository userRepository, ClockPort clock,
                                 IdGeneratorPort idGenerator) {
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.clock = Objects.requireNonNull(clock);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    public Shipment execute(User operator, OrderId orderId, WarehouseId warehouseId) {
        operator.requireRole(UserRole.LOGISTICS_OPERATOR);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        boolean alreadyCreated = shipmentRepository.findByOrderId(orderId).stream()
            .anyMatch(shipment -> shipment.getWarehouseId().equals(warehouseId));
        if (alreadyCreated) {
            throw new DuplicateResourceException(
                "Order " + orderId + " already has a shipment from warehouse " + warehouseId);
        }
        if (!(userRepository.findById(order.getBuyerId()).orElse(null) instanceof Buyer buyer)) {
            throw new BusinessRuleException("The buyer of order " + orderId + " does not exist");
        }
        return shipmentRepository.save(Shipment.prepareFor(order, ShipmentId.of(idGenerator.newId()),
            warehouseId, buyer.getDefaultShippingAddress(), clock.now()));
    }
}
