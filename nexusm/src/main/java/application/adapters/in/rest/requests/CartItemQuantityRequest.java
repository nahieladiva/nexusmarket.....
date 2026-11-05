package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemQuantityRequest(@NotNull @Positive Integer quantity) {
}
