package application.domain.exceptions;

/**
 * Se lanza cuando un token JWT es inválido, expiró o fue revocado.
 */
public class InvalidTokenException extends DomainException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
