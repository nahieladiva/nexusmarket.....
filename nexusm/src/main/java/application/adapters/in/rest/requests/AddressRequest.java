package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Address DTO for REST input.
 */
public record AddressRequest(@NotBlank String street, @NotBlank String city, @NotBlank String state,
                             @NotBlank String zipCode, @NotBlank String country) {
}
