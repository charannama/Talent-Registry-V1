package com.zencube.registry.opening.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DeadlineExpiredException extends BusinessException {
    public DeadlineExpiredException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "DEADLINE_EXPIRED");
    }
}
