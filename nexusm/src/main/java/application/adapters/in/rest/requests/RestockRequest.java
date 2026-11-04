package application.adapters.in.rest.requests;

public record RestockRequest(String productId, String warehouseId, int quantity, String reason) {
}
