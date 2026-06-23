package com.zencube.registry.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Generic standard response wrapper for all successful API responses.
 * Enforces a consistent envelope structure across the entire API surface.
 *
 * @param <T> the type of the payload
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** HTTP-level status string (e.g. "OK", "CREATED"). */
    private final String status;

    /** Human-readable description of the outcome. */
    private final String message;

    /** Server-side UTC timestamp at which the response was generated. */
    @Builder.Default
    private final Instant timestamp = Instant.now();

    /** The actual response payload. May be null for void operations. */
    private final T data;

    /** Optional pagination metadata when data is a page of results. */
    private final PaginationMeta pagination;

    // -------------------------------------------------------------------------
    // Static factory helpers
    // -------------------------------------------------------------------------

    /**
     * Successful response with a payload.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("OK")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Successful response without a payload (e.g. DELETE operations).
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status("OK")
                .message(message)
                .build();
    }

    /**
     * Successful creation response (HTTP 201).
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return ApiResponse.<T>builder()
                .status("CREATED")
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Successful paginated response.
     */
    public static <T> ApiResponse<T> paginated(String message, T data, PaginationMeta pagination) {
        return ApiResponse.<T>builder()
                .status("OK")
                .message(message)
                .data(data)
                .pagination(pagination)
                .build();
    }

    // -------------------------------------------------------------------------
    // Nested types
    // -------------------------------------------------------------------------

    /**
     * Pagination metadata embedded in paginated responses.
     */
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationMeta {
        private final int pageNumber;
        private final int pageSize;
        private final long totalElements;
        private final int totalPages;
        private final boolean last;
    }
}
