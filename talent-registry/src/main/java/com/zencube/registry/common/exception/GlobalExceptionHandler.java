package com.zencube.registry.common.exception;

import com.zencube.registry.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global exception handler that converts all application and framework exceptions
 * into a standardised {@link ErrorResponse} with the correct HTTP status code.
 *
 * <p>Handler resolution order (most-specific first):
 * <ol>
 *   <li>Custom {@link BaseException} subtypes</li>
 *   <li>Spring Validation exceptions ({@code @Valid}, {@code @Validated})</li>
 *   <li>Spring Security exceptions</li>
 *   <li>Spring MVC / HTTP exceptions</li>
 *   <li>Generic fallback ({@link Exception})</li>
 * </ol>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------------------------
    // Custom exception hierarchy
    // -------------------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(
            BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        log.warn("Unauthorized: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex, HttpServletRequest request) {
        log.warn("Invalid Token: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex, HttpServletRequest request) {
        log.warn("Forbidden: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex, HttpServletRequest request) {
        log.warn("Conflict: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        log.warn("Validation failed: {} | path={}", ex.getMessage(), request.getRequestURI());
        ErrorResponse body = ErrorResponse.of(
                ex.getHttpStatus().value(),
                ex.getErrorCode(),
                ex.getMessage(),
                request.getRequestURI(),
                ex.getFieldErrors().isEmpty() ? null : ex.getFieldErrors()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(body);
    }

    /** Catch-all for any other BaseException subtype not handled above. */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request) {
        log.error("Application error: {} | path={}", ex.getMessage(), request.getRequestURI(), ex);
        return buildResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), request);
    }

    // -------------------------------------------------------------------------
    // Spring Validation (@Valid / @Validated)
    // -------------------------------------------------------------------------

    /**
     * Handles bean validation failures on {@code @RequestBody} objects.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.computeIfAbsent(fe.getField(), k -> new ArrayList<>())
                    .add(fe.getDefaultMessage());
        }
        log.warn("Validation failed on request body | path={} | fields={}",
                request.getRequestURI(), fieldErrors.keySet());
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed. Please check the submitted data.",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Handles constraint violations on {@code @PathVariable} / {@code @RequestParam}.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, List<String>> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            String field = cv.getPropertyPath().toString();
            fieldErrors.computeIfAbsent(field, k -> new ArrayList<>())
                    .add(cv.getMessage());
        }
        log.warn("Constraint violation | path={}", request.getRequestURI());
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "CONSTRAINT_VIOLATION",
                "One or more parameters failed validation.",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    // -------------------------------------------------------------------------
    // Spring Security
    // -------------------------------------------------------------------------

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {
        log.warn("Bad credentials: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Invalid email or password.", request);
    }

    @ExceptionHandler({AuthenticationException.class, InsufficientAuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication failure: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Authentication is required to access this resource.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Access denied. You do not have permission to perform this action.", request);
    }

    // -------------------------------------------------------------------------
    // Spring MVC / HTTP infrastructure
    // -------------------------------------------------------------------------

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
            NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "The requested endpoint does not exist: " + ex.getRequestURL(), request);
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No resource found: {} | path={}", ex.getMessage(), request.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "The requested endpoint does not exist: " + request.getRequestURI(), request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not supported: {} | path={}", ex.getMethod(), request.getRequestURI());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.", request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("Media type not supported: {} | path={}", ex.getContentType(), request.getRequestURI());
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Media type '" + ex.getContentType() + "' is not supported.", request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Message not readable | path={}", request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Request body is missing or malformed.", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing request parameter: {} | path={}", ex.getParameterName(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Required parameter '" + ex.getParameterName() + "' is missing.", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for parameter '{}': {} | path={}",
                ex.getName(), ex.getValue(), request.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'.", request);
    }

    // -------------------------------------------------------------------------
    // Fallback
    // -------------------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtExceptions(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception | path={}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later. Details: " + ex.getMessage() + " - " + (ex.getCause() != null ? ex.getCause().getMessage() : ""), request);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String errorCode,
                                                         String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), errorCode, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
