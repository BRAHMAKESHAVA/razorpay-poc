package org.backend.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.backend.validation.validator.GenderValidator;

import java.lang.annotation.*;

/**
 * Custom validation annotation for gender fields.
 * Ensures that the gender value is valid according to the GenderValidator logic.
 * Can be applied to fields to enforce gender validation constraints.
 */
@Documented
@Constraint(validatedBy = GenderValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGender {

    /**
     * The error message to display when validation fails.
     *
     * @return the default error message
     */
    String message() default "Invalid gender";

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