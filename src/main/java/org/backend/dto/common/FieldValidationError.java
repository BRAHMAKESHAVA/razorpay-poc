package org.backend.dto.common;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FieldValidationError {
    private String field;
    private String error;
}
