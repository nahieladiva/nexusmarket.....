package application.adapters.in.rest.security;

import application.domain.enums.UserRole;
import application.domain.valueobjects.UserId;

/**
 * Authenticated user of the current request (it comes from the JWT).
 * Controllers receive it with {@code @AuthenticationPrincipal}.
 */
public record AuthenticatedUser(UserId userId, UserRole role, String token) {
}
