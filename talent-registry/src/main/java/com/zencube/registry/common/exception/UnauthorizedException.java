package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when an unauthenticated request attempts to access a protected resource (HTTP 401).
 */
public class UnauthorizedException extends BaseException {

    private static final String ERROR_CODE = "UNAUTHORIZED";

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public UnauthorizedException() {
        super("Authentication is required to access this resource.",
                HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }
}
