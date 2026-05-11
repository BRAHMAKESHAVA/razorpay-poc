package org.backend.dto.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO  {
    private Long userId;
    private Long customerId;
    private String accessToken;
    private String refreshToken;
    private Boolean isProfileComplete;
}
