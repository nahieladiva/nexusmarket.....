package application.domain.enums;

import application.domain.exceptions.ResourceStateConflictException;

/**
 * Status of an issued JWT. A revoked token is rejected even if it has not expired,
 * which is how logout is enforced.
 *
 * <p>Allowed transitions: {@code ACTIVE} -> {@code REVOKED}. {@code REVOKED} is terminal.</p>
 */
public enum TokenStatus {

    ACTIVE,
    REVOKED;

    /**
     * Tells whether the token can still authenticate requests.
     */
    public boolean isUsable() {
        return this == ACTIVE;
    }

    /**
     * Validates that the token can be revoked.
     */
    public void requireCanRevoke() {
        if (this == REVOKED) {
            throw new ResourceStateConflictException("Token is already revoked");
        }
    }
}
