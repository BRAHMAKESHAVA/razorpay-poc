package org.backend.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.backend.validation.validator.LongitudeValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LongitudeValidator.class) // links annotation to validator
@Target(ElementType.FIELD) // used on fields
@Retention(RetentionPolicy.RUNTIME) // available at runtime
public @interface ValidLongitude {

    String message() default "Invalid longitude"; // fallback message

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
