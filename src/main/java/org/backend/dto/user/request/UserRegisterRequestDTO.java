package org.backend.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.backend.enums.Role;
import org.backend.validation.annotation.ValidAge;
import org.backend.validation.annotation.ValidEmail;
import org.backend.validation.annotation.ValidGender;
import org.backend.validation.annotation.ValidName;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequestDTO {

    //@NotBlank(message = "First Name is required")
    //@Size(min = 2, max = 20, message = "First name must be between 2 and 20 characters")
    //@Pattern(regexp = "$|^[A-Za-z]+( [A-Za-z]+)*$", message ="Only letters and single spaces allowed")
    @ValidName(field = "First name")
    private String firstName;

    //@NotBlank(message = "Last Name is required")
    //@Size(min = 2, max = 20, message = "Last name must be between 2 and 20 characters")
    //@Pattern(regexp = "$|^[A-Za-z]+( [A-Za-z]+)*$", message ="Only letters and single spaces allowed")
    @ValidName(field = "Last name")
    private String lastName;

    //@NotBlank(message = "Gender is required")
    //@Pattern(regexp = "$|^[A-Za-z]+$", message ="Gender must contain only letters.")
    @ValidGender
    private String gender;

    //@NotNull(message = "Age is required")
    //@Min(value = 18, message = "Age must be >= 18")
    //@Max(value = 100, message = "Age must be <= 100")
    @ValidAge
    //private Integer age;
    private String age;

    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experience;

    @NotNull(message = "Role is required")
    private Role role;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "$|^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    //@Email(message = "Invalid email format")
    //@Pattern(regexp = "$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Please enter a valid email address")
    //@NotBlank(message = "Email is required")
    @ValidEmail(field = "Email")
    private String email;

    @Pattern(regexp = "$|^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters long and include uppercase, lowercase, number, and special character"
    )
    private String password;

}



