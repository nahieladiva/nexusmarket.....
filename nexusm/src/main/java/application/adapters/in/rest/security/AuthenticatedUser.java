package application.adapters.in.rest.security;

import application.domain.enums.UserRole;
import application.domain.valueobjects.UserId;

/**
 * Usuario autenticado de la petición actual (sale del JWT).
 * Se recibe en los controladores con {@code @AuthenticationPrincipal}.
 */
public record AuthenticatedUser(UserId userId, UserRole role, String token) {
}
