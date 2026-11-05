package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateReturnRequest(@NotBlank String productId, @NotNull @Positive Integer quantity,
                                  @NotBlank String reason) {
}
