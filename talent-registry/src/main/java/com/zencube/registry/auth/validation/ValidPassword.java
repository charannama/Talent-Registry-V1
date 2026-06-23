package com.zencube.registry.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom Bean Validation constraint that enforces the password policy
 * defined in {@link com.zencube.registry.common.Constants#PASSWORD_REGEX}.
 *
 * <p>Usage:
 * <pre>{@code
 *   @ValidPassword
 *   String password;
 * }</pre>
 */
@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default
        "Password must be 8–128 characters and contain at least one uppercase letter, "
        + "one lowercase letter, one digit, and one special character (@$!%*?&).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
