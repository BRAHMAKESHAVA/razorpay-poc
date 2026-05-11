package org.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.backend.enums.AddressType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    // Foreign Key
    //@NotNull(message = "Customer ID cannot be null")
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 40)
    @Column(name = "customer_name", nullable = false, length = 40)
    private String customerName;

    @NotBlank(message = "House number is required")
    @Size(max = 20)
    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Size(max = 40)
    @Column(name = "building", length = 40)
    private String buildingName;

    @Size(max = 70)
    @Column(name = "area", length = 70)
    private String area;

    @Size(max = 40)
    @Column(name = "landmark", length = 40)
    private String landmark;

    @NotBlank(message = "City is required")
    @Size(max = 30)
    @Column(name = "city", nullable = false, length = 30)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 40)
    @Column(name = "state", nullable = false, length = 40)
    private String state;

    @Size(max = 10)
    @Column(name = "country_code", length = 10)
    private String countryCode;

    @NotBlank(message = "Pin code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code. It must be a 6-digit number")
    @Size(max = 10)
    @Column(name = "pin_code", nullable = false, length = 10)
    private String pinCode;

    @NotNull(message = "Latitude is required")
    @Digits(integer = 2, fraction = 6, message = "Latitude must have up to 2 digits before decimal and 6 after")
    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    @Column(name = "latitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    @Digits(integer = 2, fraction = 6, message = "Longitude must have up to 2 digits before decimal and 6 after")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    @Column(name = "longitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal longitude;

    @NotNull(message = "Address type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false)
    private AddressType addressType;

    @NotBlank(message = "Label name is required")
    @Size(max = 100)
    @Column(name = "label_name", nullable = false, length = 100)
    private String labelName;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}