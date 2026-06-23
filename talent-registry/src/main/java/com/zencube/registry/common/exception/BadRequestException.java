package com.zencube.registry.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when the client sends an invalid or malformed request (HTTP 400).
 */
public class BadRequestException extends BaseException {

    private static final String ERROR_CODE = "BAD_REQUEST";

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE, cause);
    }
}
