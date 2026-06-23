package com.zencube.registry.common.exception;

import com.zencube.registry.common.exception.BaseException;
import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule or validation fails.
 */
public class BusinessException extends BaseException {

    private static final String DEFAULT_ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, DEFAULT_ERROR_CODE);
    }

    public BusinessException(String message, HttpStatus status) {
        super(message, status, DEFAULT_ERROR_CODE);
    }
    
    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }
}
