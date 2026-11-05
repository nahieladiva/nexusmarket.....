package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateShipmentRequest(@NotBlank String orderId, @NotBlank String warehouseId) {
}
