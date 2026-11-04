package application.domain.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Resultado de un inicio de sesión exitoso.
 */
public record AuthSession(String token, LocalDateTime expiresAt, User user) {

    public AuthSession {
        Objects.requireNonNull(token, "token es obligatorio");
        Objects.requireNonNull(expiresAt, "expiresAt es obligatorio");
        Objects.requireNonNull(user, "user es obligatorio");
    }
}
