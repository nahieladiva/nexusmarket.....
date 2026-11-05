package application.domain.exceptions;

/**
 * Thrown when the current state of a resource does not allow the operation, such as an invalid
 * status transition. The same request may be valid when the resource is in another state.
 */
public class ResourceStateConflictException extends BusinessRuleException {

    public ResourceStateConflictException(String message) {
        super(message);
    }
}
