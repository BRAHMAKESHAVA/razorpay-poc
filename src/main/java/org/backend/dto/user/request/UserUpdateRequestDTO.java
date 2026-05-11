package org.backend.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.backend.validation.annotation.ValidGender;

@Data
public class UserUpdateRequestDTO {

    //@Pattern(regexp = "$|^[A-Za-z]+( [A-Za-z]+)*$", message ="First name must contain only letters.")
    private String firstName;

    //@Pattern(regexp = "$|^[A-Za-z]+( [A-Za-z]+)*$", message ="Last name must contain only letters.")
    private String lastName;

    //@Pattern(regexp = "$|^[A-Za-z]+$", message ="Gender must contain only letters.")
    @ValidGender
    private String gender;

    @Min(value = 18, message = "Age must be >= 18")
    @Max(value = 100, message = "Age must be <= 100")
    //@ValidAge
    private Integer age;
    //private String age;

    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Please enter a valid email address")
    private String email;
}




