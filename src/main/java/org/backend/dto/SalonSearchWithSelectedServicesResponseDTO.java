package org.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalonSearchWithSelectedServicesResponseDTO {

    private Long salonId;

    private String salonName;

    private String salonImageUrl;

    private Double distance;

    private String unit;

    private BigDecimal totalPrice;

    private List<ServiceDTO> serviceDetails;
}