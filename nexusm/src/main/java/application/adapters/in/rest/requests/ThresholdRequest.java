package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ThresholdRequest(@NotBlank String productId, @NotBlank String warehouseId,
                               @NotNull @PositiveOrZero Integer reorderThreshold) {
}
