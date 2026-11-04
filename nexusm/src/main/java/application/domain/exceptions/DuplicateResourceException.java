package application.domain.exceptions;

/**
 * Thrown when trying to create a resource that already exists (email, product code...).
 */
public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
