package org.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalonDetailsDTO {

    private Long salonId;

    private Long partnerId;

    private String salonName;

    private Double latitude;

    private Double longitude;

    private String addressLine1;

    private String addressLine2;

    private String landmark;

    private String city;

    private String state;

    private String zipCode;

    private String country;

    private String workingDays;

    private LocalTime workingHoursStart;

    private LocalTime workingHoursEnd;

    private Double distance;

    private String unit;

    private String salonImage;

}