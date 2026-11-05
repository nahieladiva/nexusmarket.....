package application.adapters.in.rest.documentation;

import application.adapters.in.rest.responses.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Responses of a protected route that reads a resource by its id.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "The resource"),
    @ApiResponse(responseCode = "400", description = ApiDocumentation.BAD_REQUEST,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "401", description = ApiDocumentation.UNAUTHORIZED,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "403", description = ApiDocumentation.FORBIDDEN,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(responseCode = "404", description = ApiDocumentation.NOT_FOUND,
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface ReadResponses {
}
