package org.backend.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.backend.validation.validator.AgeValidator;

import java.lang.annotation.*;

/**
 * Custom validation annotation for age fields.
 * Ensures that the age value is valid according to the AgeValidator logic.
 * Can be applied to fields to enforce age validation constraints.
 */
@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAge {

    /**
     * The error message to display when validation fails.
     *
     * @return the default error message
     */
    String message() default "Invalid age";

    /**
     * Groups for validation constraints.
     *
     * @return the groups array
     */
    Class<?>[] groups() default {};

    /**
     * Payload for carrying metadata with the constraint.
     *
     * @return the payload array
     */
    Class<? extends Payload>[] payload() default {};
}