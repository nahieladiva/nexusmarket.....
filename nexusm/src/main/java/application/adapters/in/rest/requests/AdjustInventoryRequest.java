package application.adapters.in.rest.requests;

/** {@code delta} positivo suma, negativo resta. El motivo es obligatorio. */
public record AdjustInventoryRequest(String productId, String warehouseId, int delta,
                                     String reason) {
}
