package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * DTO to create an inventory record.
 */
public record CreateInventoryRequest(@NotBlank String productId, @NotBlank String warehouseId,
                                     @NotNull @PositiveOrZero Integer onHand,
                                     @NotNull @PositiveOrZero Integer reorderThreshold,
                                     @NotNull @Valid WarehouseLocationRequest location) {
}
