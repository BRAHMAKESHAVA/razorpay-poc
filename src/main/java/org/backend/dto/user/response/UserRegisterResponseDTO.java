package org.backend.dto.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.backend.enums.Role;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRegisterResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String gender;
    private Integer age;
    private Integer experience;
    private Role role;
    private String mobile;
    private String email;
    private String password;
}
