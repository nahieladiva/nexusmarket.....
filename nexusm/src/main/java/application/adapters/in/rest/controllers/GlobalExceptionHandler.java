package application.adapters.in.rest.controllers;

import application.adapters.in.rest.responses.ErrorResponse;
import application.domain.exceptions.BusinessRuleException;
import application.domain.exceptions.DuplicateResourceException;
import application.domain.exceptions.ForbiddenActionException;
import application.domain.exceptions.InsufficientStockException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.InvalidTokenException;
import application.domain.exceptions.OrderStateTransitionException;
import application.domain.exceptions.ResourceNotFoundException;
import application.domain.exceptions.ResourceStateConflictException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into HTTP responses, always with the {@link ErrorResponse} format.
 *
 * <p>Spring picks the most specific handler, so {@link ResourceStateConflictException} answers 409
 * although it extends {@link BusinessRuleException}, which answers 422.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameter(MethodArgumentTypeMismatchException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter " + ex.getName(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "The request body is missing or malformed", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Missing parameter " + ex.getParameterName(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidValue(IllegalArgumentException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenActionException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenActionException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(NoResourceFoundException ex,
                                                       HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Route not found", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type", request);
    }

    @ExceptionHandler({DuplicateResourceException.class, ResourceStateConflictException.class,
        OrderStateTransitionException.class, InsufficientStockException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex,
                                                        HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** A unique constraint of the database failed: two requests raced past the domain checks. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseConflict(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "The operation conflicts with existing data", request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex,
                                                            HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), request);
    }

    /** Two requests changed the same row at once: the second one lost and can try again. */
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentUpdate(ConcurrencyFailureException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.CONFLICT,
            "The resource was changed by another request; read it and try again", request);
    }

    /** The client closed the connection: it is not a server error and nobody can read an answer. */
    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public void handleClientDisconnected(AsyncRequestNotUsableException ex) {
        log.debug("The client disconnected: {}", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error at {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(), status.value(), status.getReasonPhrase(),
            message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
