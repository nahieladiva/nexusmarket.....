package application.domain.exceptions;

/**
 * Thrown when a JWT token is invalid, expired or revoked.
 */
public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
