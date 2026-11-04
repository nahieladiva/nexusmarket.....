package application.domain.services;

import application.domain.exceptions.BusinessRuleException;

/**
 * Password rule: at least {@value #MIN_LENGTH} characters, with at least one letter
 * and one digit.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    public void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new BusinessRuleException(
                "Password must be at least " + MIN_LENGTH + " characters long");
        }
        boolean hasLetter = rawPassword.chars().anyMatch(Character::isLetter);
        boolean hasDigit = rawPassword.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessRuleException(
                "Password must contain at least one letter and one digit");
        }
    }
}
