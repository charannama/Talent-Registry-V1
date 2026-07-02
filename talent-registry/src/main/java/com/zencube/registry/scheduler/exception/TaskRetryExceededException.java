package com.zencube.registry.scheduler.exception;

public class TaskRetryExceededException extends RuntimeException {
    public TaskRetryExceededException(String message) {
        super(message);
    }
}
