package org.backend.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.backend.validation.annotation.ValidLongitude;

import java.math.BigDecimal;

public class LongitudeValidator implements ConstraintValidator<ValidLongitude, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation(); // disable default message

        if (value == null) { // required check
            context.buildConstraintViolationWithTemplate("Longitude is required")
                    .addConstraintViolation();
            return false;
        }

        else if (value.compareTo(BigDecimal.valueOf(-180)) < 0) { // min range
            context.buildConstraintViolationWithTemplate("Longitude must be >= -180")
                    .addConstraintViolation();
            return false;
        }

        else if (value.compareTo(BigDecimal.valueOf(180)) > 0) { // max range
            context.buildConstraintViolationWithTemplate("Longitude must be <= 180")
                    .addConstraintViolation();
            return false;
        }

        else { // digits check only if range is valid

            int integerDigits = value.precision() - value.scale(); // digits before decimal
            int fractionDigits = value.scale(); // digits after decimal

            if (integerDigits > 3 || fractionDigits > 6) { // longitude allows 3 integer digits
                context.buildConstraintViolationWithTemplate(
                        "Longitude must have up to 3 digits before decimal and 6 after"
                ).addConstraintViolation();
                return false;
            }
        }

        return true; // valid case
    }
}