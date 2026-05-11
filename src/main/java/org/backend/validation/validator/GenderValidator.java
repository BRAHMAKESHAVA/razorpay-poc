package org.backend.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.backend.validation.annotation.ValidGender;

public class GenderValidator implements ConstraintValidator<ValidGender, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (value == null || value.trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Gender is required")
                    .addConstraintViolation();
            return false;
        }

        String cleaned = value.trim();

        if (!cleaned.matches("^[A-Za-z]+$")) {
            context.buildConstraintViolationWithTemplate("Gender must contain only letters")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}