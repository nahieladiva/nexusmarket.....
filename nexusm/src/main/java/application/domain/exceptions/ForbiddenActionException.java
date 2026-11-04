package application.domain.exceptions;

/**
 * Thrown when an authenticated user attempts an action outside their permissions
 * (for example, editing another seller's product).
 */
public class ForbiddenActionException extends DomainException {

    public ForbiddenActionException(String message) {
        super(message);
    }
}
