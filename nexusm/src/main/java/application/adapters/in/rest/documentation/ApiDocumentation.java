package application.adapters.in.rest.documentation;

/**
 * Names shared by the OpenAPI annotations of the controllers.
 */
public final class ApiDocumentation {

    /** Name of the Bearer JWT scheme, which the "Authorize" button of Swagger UI uses. */
    public static final String BEARER_AUTH = "bearerAuth";

    public static final String BAD_REQUEST = "The body, a parameter or an id is invalid";
    public static final String UNAUTHORIZED = "The token is missing, invalid, expired or revoked";
    public static final String FORBIDDEN = "The role cannot use this route, or the resource belongs to another user";
    public static final String NOT_FOUND = "The resource does not exist";
    public static final String CONFLICT = "The resource is duplicated, its state or stock does not allow the operation, "
        + "or another request changed it first";
    public static final String BUSINESS_RULE = "The operation breaks a business rule";

    private ApiDocumentation() {
    }
}
