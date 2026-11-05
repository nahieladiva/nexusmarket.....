package application.adapters.in.rest.requests;

import application.domain.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateStaffUserRequest(@NotNull UserRole role, @NotBlank String fullName,
                                     @NotBlank String email, @NotBlank String phone,
                                     @NotBlank String password) {
}
