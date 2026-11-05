package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** A positive {@code delta} adds and a negative one subtracts. The reason is required. */
public record AdjustInventoryRequest(@NotBlank String productId, @NotBlank String warehouseId,
                                     @NotNull Integer delta, @NotBlank String reason) {
}
