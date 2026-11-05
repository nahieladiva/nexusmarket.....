package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.CreateInventoryRequest;
import application.adapters.in.rest.requests.WarehouseLocationRequest;
import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.StockMovementResponse;
import application.domain.models.Inventory;
import application.domain.models.StockMovement;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import org.springframework.stereotype.Component;

/**
 * Maps between REST DTOs and the inventory domain model.
 */
@Component
public class InventoryMapper {

    public Inventory toDomain(CreateInventoryRequest request) {
        return Inventory.create(
            ProductId.of(request.productId()),
            WarehouseId.of(request.warehouseId()),
            Quantity.of(request.onHand()),
            Quantity.of(request.reorderThreshold()),
            toDomain(request.location()));
    }

    public WarehouseLocation toDomain(WarehouseLocationRequest location) {
        return WarehouseLocation.of(location.aisle(), location.shelf(), location.bin());
    }

    public StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
            movement.id(),
            movement.type().name(),
            movement.delta(),
            movement.resultingOnHand().getValue(),
            movement.reason(),
            movement.performedBy() == null ? null : movement.performedBy().toString(),
            movement.reference(),
            movement.occurredAt());
    }

    public InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
            inventory.getId().toString(),
            inventory.getProductId().toString(),
            inventory.getWarehouseId().toString(),
            inventory.getOnHand().getValue(),
            inventory.getReorderThreshold().getValue(),
            inventory.getLocation().getAisle(),
            inventory.getLocation().getShelf(),
            inventory.getLocation().getBin());
    }
}