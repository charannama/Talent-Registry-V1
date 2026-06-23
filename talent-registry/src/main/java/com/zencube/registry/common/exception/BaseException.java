package com.zencube.registry.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Root of the application-specific exception hierarchy.
 * All custom exceptions should extend this class.
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /** HTTP status code to be returned to the client. */
    private final HttpStatus httpStatus;

    /** Short error type label (e.g., "NOT_FOUND", "BAD_REQUEST"). */
    private final String errorCode;

    protected BaseException(String message, HttpStatus httpStatus, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    protected BaseException(String message, HttpStatus httpStatus, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }
}
