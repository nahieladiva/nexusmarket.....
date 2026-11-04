package application.domain.exceptions;

/**
 * Se lanza cuando una operación viola una regla de negocio del marketplace.
 */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
