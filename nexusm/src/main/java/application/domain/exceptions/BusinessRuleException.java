package application.domain.exceptions;

/**
 * Thrown when an operation violates a marketplace business rule.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
