package org.backend.dto.auth.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SendOtpResponse{
    private String message;
    private int expiryMinutes;
}
