package org.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;



@Entity
@Table(name = "service_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = false)
public class ServiceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    // FK reference to salon table
    @NotNull(message = "Salon ID is required")
    @Column(name = "salon_id", nullable = false)
    private Long salonId;

    @NotBlank(message = "Category name cannot be empty")
    @Size(max = 100, message = "Category name must be less than 100 characters")
    @Column(name = "category_name", length = 100, nullable = false)
    private String categoryName;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // default value
}