package application.domain.services;

import application.domain.exceptions.BusinessRuleException;

/**
 * Regla de contraseñas: mínimo {@value #MIN_LENGTH} caracteres, con al menos una letra
 * y un número.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new BusinessRuleException(
                "La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessRuleException(
                "La contraseña debe contener al menos una letra y un número");
        }
    }
}
