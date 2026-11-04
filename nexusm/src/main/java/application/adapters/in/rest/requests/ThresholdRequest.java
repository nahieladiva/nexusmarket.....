package application.adapters.in.rest.requests;

public record ThresholdRequest(String productId, String warehouseId, int reorderThreshold) {
}
