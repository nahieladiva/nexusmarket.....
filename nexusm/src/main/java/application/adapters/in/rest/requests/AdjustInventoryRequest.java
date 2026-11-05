package application.adapters.in.rest.requests;

/** A positive {@code delta} adds and a negative one subtracts. The reason is required. */
public record AdjustInventoryRequest(String productId, String warehouseId, int delta,
                                     String reason) {
}
