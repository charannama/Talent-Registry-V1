package com.zencube.registry.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Thrown when programmatic (business-rule) validation fails (HTTP 422).
 * Carries an optional field-level error map for structured validation feedback.
 */
@Getter
public class ValidationException extends BaseException {

    private static final String ERROR_CODE = "VALIDATION_FAILED";

    /**
     * Field-level error details.
     * Key = field name, Value = list of violation messages for that field.
     */
    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, ERROR_CODE);
        this.fieldErrors = Collections.emptyMap();
    }

    public ValidationException(String message, Map<String, List<String>> fieldErrors) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, ERROR_CODE);
        this.fieldErrors = fieldErrors != null ? Collections.unmodifiableMap(fieldErrors) : Collections.emptyMap();
    }

    /**
     * Convenience constructor for a single-field violation.
     *
     * @param field   the field that failed validation
     * @param message the violation message
     */
    public ValidationException(String field, String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, ERROR_CODE);
        this.fieldErrors = Map.of(field, List.of(message));
    }
}
