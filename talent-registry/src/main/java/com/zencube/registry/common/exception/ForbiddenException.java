package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an authenticated user lacks the required permissions for an operation (HTTP 403).
 */
public class ForbiddenException extends BaseException {

    private static final String ERROR_CODE = "FORBIDDEN";

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }

    public ForbiddenException() {
        super("Access denied. You do not have permission to perform this action.",
                HttpStatus.FORBIDDEN, ERROR_CODE);
    }
}
