package application.domain.exceptions;

/**
 * Se lanza cuando se intenta crear un recurso que ya existe (email, código de producto...).
 */
public class DuplicateResourceException extends DomainException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
