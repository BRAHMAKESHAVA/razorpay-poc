package org.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateServiceRequest {

    @NotNull(message = "Salon ID is required in body to update the service.")
    private Long salonId;
    private Long categoryId;
    private String serviceName;
    private Integer durationMinutes;
    private Integer bufferMinutes;
    private BigDecimal price;
    private Boolean isActive;
}