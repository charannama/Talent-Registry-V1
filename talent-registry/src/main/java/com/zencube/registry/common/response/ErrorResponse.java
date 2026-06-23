package com.zencube.registry.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response structure returned for all exception scenarios.
 * Provides a consistent, RFC-7807-inspired problem detail structure.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** UTC timestamp when the error occurred. */
    @Builder.Default
    private final Instant timestamp = Instant.now();

    /** HTTP status code (e.g. 400, 404, 500). */
    private final int status;

    /** Short error classification (e.g. "NOT_FOUND", "BAD_REQUEST"). */
    private final String error;

    /** Human-readable error message safe to expose to clients. */
    private final String message;

    /** The request path that triggered the error. */
    private final String path;

    /** Correlation / trace ID for log correlation across services. */
    private final String traceId;

    /**
     * Field-level validation errors.
     * Key = field name, Value = list of constraint violations on that field.
     */
    private final Map<String, List<String>> fieldErrors;

    // -------------------------------------------------------------------------
    // Static factory helpers
    // -------------------------------------------------------------------------

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .build();
    }

    public static ErrorResponse of(int status, String error, String message, String path,
                                   Map<String, List<String>> fieldErrors) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .fieldErrors(fieldErrors)
                .build();
    }

    public static ErrorResponse withTrace(int status, String error, String message,
                                          String path, String traceId) {
        return ErrorResponse.builder()
                .status(status)
                .error(error)
                .message(message)
                .path(path)
                .traceId(traceId)
                .build();
    }
}
