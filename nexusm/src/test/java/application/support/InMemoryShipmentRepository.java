package application.support;

import application.domain.models.Shipment;
import application.domain.ports.out.ShipmentRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory shipment repository for tests.
 */
public class InMemoryShipmentRepository implements ShipmentRepository {

    private final Map<ShipmentId, Shipment> shipments = new LinkedHashMap<>();

    @Override
    public Shipment save(Shipment shipment) {
        shipments.put(shipment.getId(), shipment);
        return shipment;
    }

    @Override
    public Optional<Shipment> findById(ShipmentId id) {
        return Optional.ofNullable(shipments.get(id));
    }

    @Override
    public List<Shipment> findByOrderId(OrderId orderId) {
        return shipments.values().stream()
            .filter(shipment -> shipment.getOrderId().equals(orderId))
            .toList();
    }

    @Override
    public List<Shipment> findAll() {
        return new ArrayList<>(shipments.values());
    }
}
