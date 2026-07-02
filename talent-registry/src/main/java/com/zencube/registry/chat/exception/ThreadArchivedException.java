package com.zencube.registry.chat.exception;

import com.zencube.registry.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ThreadArchivedException extends BusinessException {
    public ThreadArchivedException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "THREAD_ARCHIVED");
    }
}
