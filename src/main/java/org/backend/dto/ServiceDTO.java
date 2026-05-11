package org.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ServiceDTO {

    private String serviceName;
    private BigDecimal price;
}