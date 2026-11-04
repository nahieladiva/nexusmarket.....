package application.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Result of a successful login.
 */
public record AuthSession(String token, LocalDateTime expiresAt, User user) {

    public AuthSession {
        Objects.requireNonNull(token, "token is required");
        Objects.requireNonNull(expiresAt, "expiresAt is required");
        Objects.requireNonNull(user, "user is required");
    }
}
