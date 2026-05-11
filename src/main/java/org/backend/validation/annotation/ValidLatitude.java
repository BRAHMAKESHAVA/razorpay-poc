package org.backend.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.backend.validation.validator.LatitudeValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = LatitudeValidator.class) // connects annotation with validator
@Target(ElementType.FIELD) // can be used on fields
@Retention(RetentionPolicy.RUNTIME) // available at runtime
public @interface ValidLatitude {

    String message() default "Invalid latitude"; // default fallback message

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}