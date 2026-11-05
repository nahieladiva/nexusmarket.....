package application.domain.services.shipment;

import application.domain.enums.UserRole;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Shipment;
import application.domain.models.User;
import application.domain.ports.out.ShipmentRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.ShipmentId;

import java.util.Objects;

/**
 * Marks a shipment as packed and ready to leave the warehouse.
 */
@DomainService
public class PackShipmentService {

    private final ShipmentRepository shipmentRepository;

    public PackShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = Objects.requireNonNull(shipmentRepository);
    }

    public Shipment execute(User operator, ShipmentId shipmentId) {
        operator.requireRole(UserRole.LOGISTICS_OPERATOR);
        Shipment shipment = shipmentRepository.findById(shipmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Shipment", shipmentId.toString()));
        shipment.pack();
        return shipmentRepository.save(shipment);
    }
}
