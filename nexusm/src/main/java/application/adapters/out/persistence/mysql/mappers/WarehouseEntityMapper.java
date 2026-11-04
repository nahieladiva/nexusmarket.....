package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.domain.enums.WarehouseStatus;
import application.domain.enums.WarehouseType;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the warehouse domain model.
 */
@Component
public class WarehouseEntityMapper {

    public WarehouseJpaEntity toEntity(Warehouse warehouse) {
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setId(warehouse.getId().toString());
        entity.setType(warehouse.getType().name());
        entity.setOwnerSellerId(warehouse.getOwnerSellerId().map(UserId::toString).orElse(null));
        entity.setName(warehouse.getName());
        entity.setStreet(warehouse.getAddress().getStreet());
        entity.setCity(warehouse.getAddress().getCity());
        entity.setState(warehouse.getAddress().getState());
        entity.setZipCode(warehouse.getAddress().getZipCode());
        entity.setCountry(warehouse.getAddress().getCountry());
        entity.setAisle(warehouse.getLocation().getAisle());
        entity.setShelf(warehouse.getLocation().getShelf());
        entity.setBin(warehouse.getLocation().getBin());
        entity.setStatus(warehouse.getStatus().name());
        return entity;
    }

    public Warehouse toDomain(WarehouseJpaEntity entity) {
        return new Warehouse(
            WarehouseId.of(entity.getId()),
            WarehouseType.valueOf(entity.getType()),
            entity.getOwnerSellerId() == null ? null : UserId.of(entity.getOwnerSellerId()),
            entity.getName(),
            new Address(entity.getStreet(), entity.getCity(), entity.getState(),
                entity.getZipCode(), entity.getCountry()),
            new WarehouseLocation(entity.getAisle(), entity.getShelf(), entity.getBin()),
            WarehouseStatus.valueOf(entity.getStatus()));
    }
}
