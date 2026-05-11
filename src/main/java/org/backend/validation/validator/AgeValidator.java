package org.backend.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.backend.validation.annotation.ValidAge;

public class AgeValidator implements ConstraintValidator<ValidAge, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();

        if (value == null || value.trim().isEmpty()) {
            context.buildConstraintViolationWithTemplate("Age is required")
                    .addConstraintViolation();
            return false;
        }

        try {
            int age = Integer.parseInt(value);

            if (age < 18 || age > 60) {
                context.buildConstraintViolationWithTemplate("Age must be between 18 and 60")
                        .addConstraintViolation();
                return false;
            }

        } catch (NumberFormatException e) {
            context.buildConstraintViolationWithTemplate("Age must be a valid number")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}