package application.adapters.in.rest.mappers;

import application.adapters.in.rest.responses.InventoryResponse;
import application.adapters.in.rest.responses.StockMovementResponse;
import application.domain.models.Inventory;
import application.domain.models.StockMovement;
import org.springframework.stereotype.Component;

/**
 * Maps the inventory domain model to REST DTOs.
 */
@Component
public class InventoryMapper {

    public StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
            movement.id(),
            movement.type(),
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
