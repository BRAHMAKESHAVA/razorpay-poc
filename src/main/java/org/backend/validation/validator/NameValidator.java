package org.backend.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.backend.validation.annotation.ValidName;

public class NameValidator implements ConstraintValidator<ValidName, String> {

    private String field;

    @Override
    public void initialize(ValidName annotation) {
        this.field = annotation.field();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (value == null || value.trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate(field + " is required")
                    .addConstraintViolation();
            return false;
        }

        String cleaned = value.trim();

        if (cleaned.length() < 2 || cleaned.length() > 20) {
            context.buildConstraintViolationWithTemplate(field + " must be between 2 and 20 characters")
                    .addConstraintViolation();
            return false;
        }

        if (!cleaned.matches("^[A-Za-z]+( [A-Za-z]+)*$")) {
            context.buildConstraintViolationWithTemplate("Only letters and single spaces allowed")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}

//===================

