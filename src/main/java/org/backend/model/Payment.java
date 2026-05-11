package org.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private Long bookingId;

    private BigDecimal amount;
    private String currency;

    private String status;
    private String provider;

    private String providerOrderId;
    private String providerPaymentId;
    private String providerSignature;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}