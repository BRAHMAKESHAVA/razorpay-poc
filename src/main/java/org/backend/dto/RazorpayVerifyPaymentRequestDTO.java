package org.backend.dto;

import lombok.Data;

@Data
public class RazorpayVerifyPaymentRequestDTO {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}