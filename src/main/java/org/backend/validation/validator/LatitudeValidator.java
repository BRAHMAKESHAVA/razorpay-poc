package org.backend.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.backend.validation.annotation.ValidLatitude;

import java.math.BigDecimal;

public class LatitudeValidator implements ConstraintValidator<ValidLatitude, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation(); // disable default message

        if (value == null) { // required check
            context.buildConstraintViolationWithTemplate("Latitude is required")
                    .addConstraintViolation();
            return false;
        }

        else if (value.compareTo(BigDecimal.valueOf(-90)) < 0) { // minimum range check
            context.buildConstraintViolationWithTemplate("Latitude must be >= -90")
                    .addConstraintViolation();
            return false;
        }

        else if (value.compareTo(BigDecimal.valueOf(90)) > 0) { // maximum range check
            context.buildConstraintViolationWithTemplate("Latitude must be <= 90")
                    .addConstraintViolation();
            return false;
        }

        else { // format check after range validation

            BigDecimal absValue = value.abs(); // remove sign for accurate digit calculation

            int integerDigits = absValue.precision() - absValue.scale(); // digits before decimal
            int fractionDigits = absValue.scale(); // digits after decimal

            if (integerDigits > 2 || fractionDigits > 6) { // latitude allows 2 integer digits
                context.buildConstraintViolationWithTemplate(
                        "Latitude must have up to 2 digits before decimal and 6 after"
                ).addConstraintViolation();
                return false;
            }
        }

        return true; // valid case
    }
}