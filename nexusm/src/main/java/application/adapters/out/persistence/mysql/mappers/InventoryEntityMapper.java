package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.InventoryJpaEntity;
import application.domain.models.Inventory;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import org.springframework.stereotype.Component;

/**
 * Maps between the JPA entity and the inventory domain model.
 */
@Component
public class InventoryEntityMapper {

    public InventoryJpaEntity toEntity(Inventory inventory) {
        InventoryJpaEntity entity = new InventoryJpaEntity();
        entity.setId(inventory.getId().toString());
        entity.setProductId(inventory.getProductId().toString());
        entity.setWarehouseId(inventory.getWarehouseId().toString());
        entity.setOnHand(inventory.getOnHand().getValue());
        entity.setReorderThreshold(inventory.getReorderThreshold().getValue());
        entity.setAisle(inventory.getLocation().getAisle());
        entity.setShelf(inventory.getLocation().getShelf());
        entity.setBin(inventory.getLocation().getBin());
        return entity;
    }

    public Inventory toDomain(InventoryJpaEntity entity) {
        return new Inventory(
            InventoryId.of(entity.getId()),
            ProductId.of(entity.getProductId()),
            WarehouseId.of(entity.getWarehouseId()),
            Quantity.of(entity.getOnHand()),
            Quantity.of(entity.getReorderThreshold()),
            new WarehouseLocation(entity.getAisle(), entity.getShelf(), entity.getBin()));
    }
}