package com.zencube.registry.chat.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DirectCommunicationNotAllowedException extends BusinessException {
    public DirectCommunicationNotAllowedException(String message) {
        super(message, HttpStatus.FORBIDDEN, "DIRECT_COMMUNICATION_NOT_ALLOWED");
    }
}
