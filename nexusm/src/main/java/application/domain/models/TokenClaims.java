package application.domain.models;

import application.domain.enums.UserRole;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data carried inside the JWT.
 */
public record TokenClaims(String tokenId, UserId userId, UserRole role,
                          LocalDateTime issuedAt, LocalDateTime expiresAt) {

    public TokenClaims {
        Objects.requireNonNull(tokenId, "tokenId is required");
        Objects.requireNonNull(userId, "userId is required");
        Objects.requireNonNull(role, "role is required");
        Objects.requireNonNull(issuedAt, "issuedAt is required");
        Objects.requireNonNull(expiresAt, "expiresAt is required");
    }
}
