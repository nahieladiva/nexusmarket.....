package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO to change the price of a product.
 */
public record PriceChangeRequest(@NotNull @Valid PriceRequest price) {
}
