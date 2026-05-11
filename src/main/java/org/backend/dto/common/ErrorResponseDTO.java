package org.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    private int status_code;
    private String status;
    private String message;
    private List<FieldValidationError> fieldErrors;
    private String error;
}
