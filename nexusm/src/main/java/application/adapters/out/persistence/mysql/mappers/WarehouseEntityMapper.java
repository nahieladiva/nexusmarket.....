package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.WarehouseJpaEntity;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import org.springframework.stereotype.Component;

/**
 * Mapea entre la entidad JPA y el modelo de dominio de almacenes.
 */
@Component
public class WarehouseEntityMapper {

    public WarehouseJpaEntity toEntity(Warehouse warehouse) {
        WarehouseJpaEntity entity = new WarehouseJpaEntity();
        entity.setId(warehouse.getId().toString());
        entity.setName(warehouse.getName());
        entity.setStreet(warehouse.getAddress().getStreet());
        entity.setCity(warehouse.getAddress().getCity());
        entity.setState(warehouse.getAddress().getState());
        entity.setZipCode(warehouse.getAddress().getZipCode());
        entity.setCountry(warehouse.getAddress().getCountry());
        entity.setAisle(warehouse.getLocation().getAisle());
        entity.setShelf(warehouse.getLocation().getShelf());
        entity.setBin(warehouse.getLocation().getBin());
        entity.setActive(warehouse.isActive());
        return entity;
    }

    public Warehouse toDomain(WarehouseJpaEntity entity) {
        return new Warehouse(
            WarehouseId.of(entity.getId()),
            entity.getName(),
            new Address(entity.getStreet(), entity.getCity(), entity.getState(),
                entity.getZipCode(), entity.getCountry()),
            new WarehouseLocation(entity.getAisle(), entity.getShelf(), entity.getBin()),
            entity.isActive());
    }
}