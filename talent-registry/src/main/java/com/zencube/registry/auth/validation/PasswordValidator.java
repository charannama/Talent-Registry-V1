package com.zencube.registry.auth.validation;

import com.zencube.registry.common.constants.Constants;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that a password satisfies the application password policy.
 *
 * <p>Policy (from {@link Constants#PASSWORD_REGEX}):
 * <ul>
 *   <li>8 to 128 characters</li>
 *   <li>At least one uppercase letter (A–Z)</li>
 *   <li>At least one lowercase letter (a–z)</li>
 *   <li>At least one digit (0–9)</li>
 *   <li>At least one special character ({@code @$!%*?&})</li>
 * </ul>
 *
 * <p>Null values are treated as valid — use {@code @NotBlank} alongside
 * {@code @ValidPassword} to reject nulls/blanks.
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // Null/blank is handled by @NotBlank — skip here
        if (password == null || password.isBlank()) {
            return true;
        }
        return password.matches(Constants.PASSWORD_REGEX);
    }
}
