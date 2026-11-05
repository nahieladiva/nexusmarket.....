package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.AddressEmbeddable;
import application.adapters.out.persistence.mysql.entities.ShipmentJpaEntity;
import application.domain.models.Shipment;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the shipment domain model.
 */
@Component
public class ShipmentEntityMapper {

    public ShipmentJpaEntity toEntity(Shipment shipment) {
        Address address = shipment.getShippingAddress();
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.setId(shipment.getId().toString());
        entity.setOrderId(shipment.getOrderId().toString());
        entity.setWarehouseId(shipment.getWarehouseId().toString());
        entity.setShippingAddress(new AddressEmbeddable(address.getStreet(), address.getCity(),
            address.getState(), address.getZipCode(), address.getCountry()));
        entity.setLogisticsOperatorId(
            shipment.getLogisticsOperatorId().map(UserId::toString).orElse(null));
        entity.setTrackingNumber(shipment.getTrackingNumber().orElse(null));
        entity.setStatus(shipment.getStatus());
        entity.setCreatedAt(shipment.getCreatedAt());
        entity.setDispatchedAt(shipment.getDispatchedAt().orElse(null));
        entity.setDeliveredAt(shipment.getDeliveredAt().orElse(null));
        return entity;
    }

    public Shipment toDomain(ShipmentJpaEntity entity) {
        AddressEmbeddable address = entity.getShippingAddress();
        return new Shipment(
            ShipmentId.of(entity.getId()),
            OrderId.of(entity.getOrderId()),
            WarehouseId.of(entity.getWarehouseId()),
            Address.of(address.getStreet(), address.getCity(), address.getState(),
                address.getZipCode(), address.getCountry()),
            entity.getLogisticsOperatorId() == null ? null : UserId.of(entity.getLogisticsOperatorId()),
            entity.getTrackingNumber(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getDispatchedAt(),
            entity.getDeliveredAt());
    }
}
