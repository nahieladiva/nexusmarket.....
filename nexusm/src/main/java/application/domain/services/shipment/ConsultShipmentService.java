package application.domain.services.shipment;

import application.domain.enums.UserRole;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.services.order.ConsultOrderService;
import application.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;

/**
 * Shipment queries: whoever can see an order can see its shipments, and internal staff see all.
 */
@DomainService
public class ConsultShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ConsultOrderService consultOrderService;

    public ConsultShipmentService(ShipmentRepository shipmentRepository,
                                  ConsultOrderService consultOrderService) {
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
        this.consultOrderService = Objects.requireNonNull(consultOrderService);
    }

    public List<Shipment> findByOrder(User actor, OrderId orderId) {
        consultOrderService.execute(actor, orderId);
        return shipmentRepository.findByOrderId(orderId);
    }

    public List<Shipment> findAll(User actor) {
        actor.requireAnyRole(UserRole.ADMINISTRATOR, UserRole.SUPERVISOR, UserRole.LOGISTICS_OPERATOR);
        return shipmentRepository.findAll();
    }
}
