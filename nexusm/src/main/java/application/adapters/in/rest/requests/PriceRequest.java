package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Price DTO (amount and currency) for REST.
 */
public record PriceRequest(@NotBlank String amount, @NotBlank String currency) {
}
