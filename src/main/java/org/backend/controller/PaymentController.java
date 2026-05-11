package org.backend.controller;

import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import org.backend.dto.RazorpayOrderResponseDTO;
import org.backend.dto.RazorpayVerifyPaymentRequestDTO;
import org.backend.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentController {

    private final PaymentService paymentService;

    // CREATE ORDER
    @PostMapping("/order/{bookingId}")
    public RazorpayOrderResponseDTO createOrder(
            @PathVariable Long bookingId
    ) {
        return paymentService.createOrder(bookingId);
    }

    // VERIFY PAYMENT
    @PostMapping("/verify/{bookingId}")
    public String verify(
            @PathVariable Long bookingId,
            @RequestBody RazorpayVerifyPaymentRequestDTO dto
    ) throws RazorpayException {
        paymentService.verifyPayment(bookingId, dto);
        return "Payment verified & Booking confirmed";
    }

    // REFUND PAYMENT
    @PostMapping("/refund/{bookingId}")
    public String refund(
            @PathVariable Long bookingId,
            @RequestParam String reason
    ) {
        paymentService.refundPayment(bookingId, reason);
        return "Refund Successful";
    }
}