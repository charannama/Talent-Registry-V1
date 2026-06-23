package com.zencube.registry.common.exception;

/**
 * Purpose: Exception thrown when attempting to create a resource that already exists.
 * Layer: Exception
 * Business Logic: Provides a standard conflict response mapping.
 * Best Practices: Use instead of generic RuntimeException for better error handling.
 */
public class DuplicateResourceException extends ConflictException {
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue));
    }
    public DuplicateResourceException(String message) {
        super(message);
    }
}
