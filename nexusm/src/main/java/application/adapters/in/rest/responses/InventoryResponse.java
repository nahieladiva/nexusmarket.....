package application.adapters.in.rest.responses;

/**
 * Inventory DTO for REST responses.
 */
public record InventoryResponse(String id, String productId, String warehouseId,
                                int onHand, int reorderThreshold,
                                String aisle, String shelf, String bin) {
}