package application.domain.exceptions;

/**
 * Se lanza cuando el email o la contraseña no son válidos al iniciar sesión.
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Email o contraseña incorrectos");
    }
}
