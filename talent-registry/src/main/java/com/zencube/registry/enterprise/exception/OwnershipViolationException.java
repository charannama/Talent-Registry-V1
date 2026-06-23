package com.zencube.registry.enterprise.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class OwnershipViolationException extends BusinessException {
    public OwnershipViolationException(String message) {
        super(message, HttpStatus.FORBIDDEN, "OWNERSHIP_VIOLATION");
    }
}
