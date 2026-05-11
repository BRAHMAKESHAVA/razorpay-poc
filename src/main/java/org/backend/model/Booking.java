package org.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private Long salonId;
    private Long customerId;
    private Long packageId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private BigDecimal grossAmount;
    private BigDecimal platformFee;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private BigDecimal partnerAmount;

    private String status;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}