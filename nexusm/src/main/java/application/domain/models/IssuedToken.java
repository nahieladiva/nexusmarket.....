package application.domain.models;

import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de un token emitido, guardado en la base de datos de tokens.
 * Permite revocar un token antes de que expire (logout).
 */
public final class IssuedToken {

    private final String tokenId;
    private final UserId userId;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private boolean revoked;

    public IssuedToken(String tokenId, UserId userId, LocalDateTime issuedAt,
                       LocalDateTime expiresAt, boolean revoked) {
        this.tokenId = Objects.requireNonNull(tokenId, "tokenId es obligatorio");
        this.userId = Objects.requireNonNull(userId, "userId es obligatorio");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt es obligatorio");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt es obligatorio");
        this.revoked = revoked;
    }

    public boolean isUsableAt(LocalDateTime moment) {
        return !revoked && moment.isBefore(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public String getTokenId() {
        return tokenId;
    }

    public UserId getUserId() {
        return userId;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
