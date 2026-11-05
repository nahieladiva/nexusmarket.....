package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record CreateSellerRequest(@NotBlank String fullName, @NotBlank String email,
                                  @NotBlank String phone, @NotBlank String password,
                                  @NotBlank String businessName) {
}
