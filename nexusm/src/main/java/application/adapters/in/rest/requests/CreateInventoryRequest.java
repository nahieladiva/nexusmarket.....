package application.adapters.in.rest.requests;

/**
 * DTO to create an inventory record.
 */
public record CreateInventoryRequest(String productId, String warehouseId,
                                     int onHand, int reorderThreshold,
                                     WarehouseLocationRequest location) {
}