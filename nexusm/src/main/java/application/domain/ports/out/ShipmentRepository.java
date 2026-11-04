package application.domain.ports.out;

import application.domain.models.Shipment;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — shipments (MySQL). An order has one shipment per warehouse.
 */
public interface ShipmentRepository {

    Shipment save(Shipment shipment);

    Optional<Shipment> findById(ShipmentId id);

    List<Shipment> findByOrderId(OrderId orderId);

    List<Shipment> findAll();
}
