package com.zencube.registry.application.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidInterviewStateException extends BusinessException {
    public InvalidInterviewStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_INTERVIEW_STATE");
    }
}
