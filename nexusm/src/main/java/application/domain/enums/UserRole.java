package application.domain.enums;

/**
 * System roles. Each user has exactly one role (RG-02), and the role defines
 * the only operations the user can perform (RG-03).
 */
public enum UserRole {

    BUYER,
    SELLER,
    LOGISTICS_OPERATOR,
    ADMINISTRATOR,
    SUPERVISOR;

    /**
     * Buyers are the only users who register themselves through the public API.
     */
    public boolean canSelfRegister() {
        return this == BUYER;
    }

    /**
     * Internal staff accounts are created by an administrator.
     */
    public boolean isInternalStaff() {
        return this == LOGISTICS_OPERATOR || this == ADMINISTRATOR || this == SUPERVISOR;
    }

    /**
     * The supervisor can consult any information but cannot modify business data.
     */
    public boolean isReadOnly() {
        return this == SUPERVISOR;
    }

    /**
     * Converts a text value (for example, from a request) into a role.
     *
     * @throws IllegalArgumentException when the value is blank or is not a valid role
     */
    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid role: " + value);
        }
    }
}
