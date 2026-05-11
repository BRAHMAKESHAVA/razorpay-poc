package org.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_refund")
@Data
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refundId;

    private Long paymentId;
    private BigDecimal refundAmount;

    private String status;
    private String reason;

    private String providerRefundId;

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}