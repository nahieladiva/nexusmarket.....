package application.domain.exceptions;

/**
 * Se lanza cuando un usuario autenticado intenta una acción que no le corresponde
 * (por ejemplo, editar el producto de otro vendedor).
 */
public class ForbiddenActionException extends DomainException {

    public ForbiddenActionException(String message) {
        super(message);
    }
}
