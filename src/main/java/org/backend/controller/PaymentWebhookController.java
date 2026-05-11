package org.backend.controller;

import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PaymentWebhookController {

    private final PaymentService paymentService;

    // HANDLE WEBHOOK
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        System.out.println("WEBHOOK HIT");

        try {
            String secret = "mysecret123";
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, secret);

            if (!isValid) {
                return ResponseEntity.badRequest().body("Invalid signature");
            }

            System.out.println("Webhook Verified");
            paymentService.handleWebhook(payload, signature);

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook failed");
        }
    }
}