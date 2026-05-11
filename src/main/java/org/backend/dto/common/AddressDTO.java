package org.backend.dto.common;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;
import org.backend.enums.AddressType;
import org.backend.validation.annotation.ValidLatitude;
import org.backend.validation.annotation.ValidLongitude;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    private Long addressId;

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 40)
    private String customerName;

    @NotBlank(message = "House number is required")
    @Size(max = 20)
    private String houseNumber;

    @Size(max = 40)
    private String buildingName;

    @Size(max = 70)
    private String area;

    @Size(max = 40)
    private String landmark;

    @NotBlank(message = "City is required")
    @Size(max = 30)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 40)
    private String state;

    @Size(max = 10)
    @NotBlank(message = "Country code is required")
    private String countryCode;

    @NotBlank(message = "Pin code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid PIN code. It must be a 6-digit number")
    @Size(max = 10)
    private String pinCode;

    //    @NotNull(message = "Latitude is required")
    //    @Digits(integer = 2, fraction = 6, message = "Latitude must have up to 2 digits before decimal and 6 after")
    //    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    //    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    @ValidLatitude
    private BigDecimal latitude;

    //    @NotNull(message = "Longitude is required")
    //    @Digits(integer = 2, fraction = 6, message = "Longitude must have up to 2 digits before decimal and 6 after")
    //    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    //    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    @ValidLongitude
    private BigDecimal longitude;

    @NotNull(message = "Address type is required")
    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    @NotBlank(message = "Label name is required")
    @Size(max = 100)
    private String labelName;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}