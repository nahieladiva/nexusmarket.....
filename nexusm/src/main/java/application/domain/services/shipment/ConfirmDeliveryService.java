package application.domain.services.shipment;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ClockPort;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ShipmentId;

import java.util.Objects;

/**
 * Confirms that the buyer received a shipment and moves the order forward when every shipment is
 * delivered.
 */
@DomainService
public class ConfirmDeliveryService {

    private final ShipmentRepository shipmentRepository;
    private final ApplyShipmentProgressService applyShipmentProgressService;
    private final ClockPort clock;

    public ConfirmDeliveryService(ShipmentRepository shipmentRepository,
                                  ApplyShipmentProgressService applyShipmentProgressService,
                                  ClockPort clock) {
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
        this.applyShipmentProgressService = Objects.requireNonNull(applyShipmentProgressService);
        this.clock = Objects.requireNonNull(clock);
    }

    public Shipment execute(User operator, ShipmentId shipmentId) {
        operator.requireRole(UserRole.LOGISTICS_OPERATOR);
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId.toString()));
        shipment.confirmDelivery(clock.now());
        Shipment saved = shipmentRepository.save(shipment);
        applyShipmentProgressService.execute(saved.getOrderId());
        return saved;
    }
}
