package com.zencube.registry.scheduler.exception;

public class TaskProcessorNotFoundException extends RuntimeException {
    public TaskProcessorNotFoundException(String message) {
        super(message);
    }
}
