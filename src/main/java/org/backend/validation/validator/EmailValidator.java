package org.backend.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.backend.validation.annotation.ValidEmail;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private String field;

    @Override
    public void initialize(ValidEmail annotation) {
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        // 1. Null / Empty Check
        if (value == null || value.trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate(field + " is required")
                    .addConstraintViolation();
            return false;
        }

        String email = value.trim();

        // 2. Length Check (optional but good practice)
        if (email.length() > 50) {
            context.buildConstraintViolationWithTemplate(field + " must not exceed 50 characters")
                    .addConstraintViolation();
            return false;
        }

        // 3. Format Check
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (!email.matches(regex)) {
            context.buildConstraintViolationWithTemplate("Please enter a valid email address")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}