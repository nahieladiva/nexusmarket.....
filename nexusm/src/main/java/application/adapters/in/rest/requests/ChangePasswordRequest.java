package application.adapters.in.rest.requests;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank String newPassword) {
}
