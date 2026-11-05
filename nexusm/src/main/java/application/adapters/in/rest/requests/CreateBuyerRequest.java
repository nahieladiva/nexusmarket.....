package application.adapters.in.rest.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** {@code Email} validates the format after normalizing it, so only presence is checked here. */
public record CreateBuyerRequest(@NotBlank String fullName, @NotBlank String email,
                                 @NotBlank String phone, @NotBlank String password,
                                 @NotNull @Valid AddressRequest defaultShippingAddress) {
}
