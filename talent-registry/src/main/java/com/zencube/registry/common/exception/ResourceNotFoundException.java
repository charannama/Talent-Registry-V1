package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource cannot be found (HTTP 404).
 *
 * <p>Usage:
 * <pre>{@code
 *   throw new ResourceNotFoundException("User", "id", userId);
 * }</pre>
 */
public class ResourceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }

    /**
     * Convenience constructor producing a message like:
     * "User not found with id: 'abc-123'"
     *
     * @param resourceName the entity type (e.g. "User")
     * @param fieldName    the lookup field (e.g. "id")
     * @param fieldValue   the value that was searched
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: '%s'", resourceName, fieldName, fieldValue),
                HttpStatus.NOT_FOUND, ERROR_CODE);
    }
}
