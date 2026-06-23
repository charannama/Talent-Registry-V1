package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an operation would violate a uniqueness or state constraint (HTTP 409).
 *
 * <p>Examples: duplicate email registration, re-submitting an already-accepted application.
 */
public class ConflictException extends BaseException {

    private static final String ERROR_CODE = "CONFLICT";

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);
    }

    /**
     * Convenience constructor for duplicate-resource conflicts.
     *
     * @param resourceName the entity type (e.g. "User")
     * @param fieldName    the conflicting field (e.g. "email")
     * @param fieldValue   the conflicting value
     */
    public ConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.CONFLICT, ERROR_CODE);
    }
}
