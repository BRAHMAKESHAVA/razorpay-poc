package org.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Long serviceId;

    // FK: salon
    @NotNull(message = "Salon ID is required")
    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    // FK: category
    @NotNull(message = "Category ID is required")
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @NotBlank(message = "Service name cannot be empty")
    @Size(max = 100, message = "Service name must be less than 100 characters")
    @Column(name = "service_name", length = 100, nullable = false)
    private String serviceName;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "buffer_minutes")
    private Integer bufferMinutes = 0; // default

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must be valid (max 10 digits, 2 decimals)")
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // default
}