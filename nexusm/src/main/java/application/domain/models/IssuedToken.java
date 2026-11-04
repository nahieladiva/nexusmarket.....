package application.domain.models;

import application.domain.enums.TokenStatus;
import application.domain.valueobjects.UserId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Record of an issued JWT, stored in the token database.
 * It allows a token to be revoked before it expires (logout).
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>The token id, owner, issue date and expiration date are immutable.</li>
 *   <li>The expiration date is after the issue date.</li>
 *   <li>The status only moves from {@code ACTIVE} to {@code REVOKED}.</li>
 * </ul>
 */
public final class IssuedToken {

    private final String tokenId;
    private final UserId userId;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private TokenStatus status;

    public IssuedToken(String tokenId, UserId userId, LocalDateTime issuedAt,
                       LocalDateTime expiresAt, TokenStatus status) {
        this.tokenId = Objects.requireNonNull(tokenId, "Token id is required");
        this.userId = Objects.requireNonNull(userId, "User id is required");
        this.issuedAt = Objects.requireNonNull(issuedAt, "Issue date is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration date is required");
        this.status = Objects.requireNonNull(status, "Token status is required");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("Expiration date must be after the issue date");
        }
    }

    /**
     * Creates the record of a token that has just been issued.
     */
    public static IssuedToken issue(String tokenId, UserId userId, LocalDateTime issuedAt,
                                    LocalDateTime expiresAt) {
        return new IssuedToken(tokenId, userId, issuedAt, expiresAt, TokenStatus.ACTIVE);
    }

    /**
     * A token is usable while it is active and has not expired.
     */
    public boolean isUsableAt(LocalDateTime moment) {
        return status.isUsable() && moment.isBefore(expiresAt);
    }

    /**
     * Revokes the token (logout).
     *
     * @throws application.domain.exceptions.BusinessRuleException when it is already revoked
     */
    public void revoke() {
        status.requireCanRevoke();
        this.status = TokenStatus.REVOKED;
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

    public TokenStatus getStatus() {
        return status;
    }
}
