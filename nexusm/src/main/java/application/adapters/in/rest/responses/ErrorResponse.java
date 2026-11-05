package application.adapters.in.rest.responses;

import java.time.LocalDateTime;

/**
 * Standard error DTO for REST responses.
 */
public record ErrorResponse(LocalDateTime timestamp, int status, String error,
                            String message, String path) {
}