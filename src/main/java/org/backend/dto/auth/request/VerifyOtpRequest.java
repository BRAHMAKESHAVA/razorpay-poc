package org.backend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.backend.enums.Role;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^$|^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    private String mobile;

    @NotNull(message = "Role is required")
    private Role role;

    @Pattern(regexp = "$|^\\d{4}$", message = "OTP must be a 4-digit number")
    @NotBlank(message = "OTP is required")
    private String otp;


}
