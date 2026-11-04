package application.domain.exceptions;

/**
 * Base domain exception. Every violated business rule
 * is represented by a subclass of this exception.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}