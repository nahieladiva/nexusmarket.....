package application.domain.exceptions;

/**
 * Thrown when the email or password is not valid at login.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Incorrect email or password");
    }
}
