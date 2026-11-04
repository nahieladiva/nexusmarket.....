package application.domain.enums;

/**
 * Roles de usuario del sistema.
 */
public enum UserRole {

    BUYER,
    SELLER,
    ADMIN;

    public static UserRole fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
        try {
            return UserRole.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Rol inválido: " + value);
        }
    }
}