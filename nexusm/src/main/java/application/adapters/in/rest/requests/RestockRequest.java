package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RestockRequest(@NotBlank String productId, @NotBlank String warehouseId,
                             @NotNull @Positive Integer quantity, String reason) {
}
