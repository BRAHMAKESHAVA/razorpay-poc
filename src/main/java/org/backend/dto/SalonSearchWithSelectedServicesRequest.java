package org.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonSearchWithSelectedServicesRequest {

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @NotNull(message = "Distance is required")
    @Positive(message = "Distance must be greater than 0")
    private Double distance;

    @NotBlank(message = "Unit is required (KM or M)")
    @Pattern(regexp = "KM|M", message = "Unit must be KM or M")
    private String unit;

    @NotEmpty(message = "At least one service must be selected")
    private List<@NotBlank(message = "Service name cannot be empty") String> serviceNames;
}