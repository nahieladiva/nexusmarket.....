package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Datos que viajan dentro del JWT.
 */
public record TokenClaims(String tokenId, UserId userId, UserRole role,
                          LocalDateTime issuedAt, LocalDateTime expiresAt) {

    public TokenClaims {
        Objects.requireNonNull(tokenId, "tokenId es obligatorio");
        Objects.requireNonNull(userId, "userId es obligatorio");
        Objects.requireNonNull(role, "role es obligatorio");
        Objects.requireNonNull(issuedAt, "issuedAt es obligatorio");
        Objects.requireNonNull(expiresAt, "expiresAt es obligatorio");
    }
}
