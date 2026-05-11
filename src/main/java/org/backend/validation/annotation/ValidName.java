//package org.backend.validation.annotation;
//
//import jakarta.validation.Constraint;
//import jakarta.validation.Payload;
//import org.backend.validation.validator.NameValidator;
//
//import java.lang.annotation.*;
//
//// Marks this as a validation annotation
//@Documented
//@Constraint(validatedBy = NameValidator.class)
//
//// Can be used on fields
//@Target({ ElementType.FIELD })
//
//// Available at runtime
//@Retention(RetentionPolicy.RUNTIME)
//public @interface ValidName {
//
//    // Dynamic field name (e.g., "First name", "Last name")
//    String field();
//
//    // Default message (not used because we override dynamically)
//    String message() default "Invalid name";
//
//    Class<?>[] groups() default {};
//    Class<? extends Payload>[] payload() default {};
//}


//====================

package org.backend.validation.annotation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.backend.validation.validator.NameValidator;


import java.lang.annotation.*;

/*
 * Custom validation annotation for name fields.
 * Ensures that the name value is valid according to the NameValidator logic.
 * Can be applied to fields to enforce name validation constraints.
 */

// Marks this as a validation annotation
@Documented
@Constraint(validatedBy = NameValidator.class)
// Can be used on fields
@Target({ ElementType.FIELD })
// Available at runtime
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidName {

    // Dynamic field name (e.g., "First name", "Last name")
    String field();

    // Default message (not used because we override dynamically)
    String message() default "Invalid name";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

