package org.backend.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.backend.validation.validator.EmailValidator;

import java.lang.annotation.*;

/*
 * Custom validation annotation for email fields.
 * Validates email format and required conditions.
 */

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

    // Dynamic field name (e.g., "Email", "User Email")
    String field();

    String message() default "Invalid email";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}