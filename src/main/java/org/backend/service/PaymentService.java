package org.backend.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import org.backend.dto.RazorpayOrderResponseDTO;
import org.backend.dto.RazorpayVerifyPaymentRequestDTO;
import org.backend.exception.BadRequestException;
import org.backend.model.Booking;
import org.backend.model.Payment;
import org.backend.model.PaymentRefund;
import org.backend.repository.BookingRepository;
import org.backend.repository.PaymentRefundRepository;
import org.backend.repository.PaymentRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final BookingService bookingService;
    private final PaymentRefundRepository refundRepo;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    // CREATE ORDER
    public RazorpayOrderResponseDTO createOrder(Long bookingId) {
        try {
            Booking booking = bookingRepo.findById(bookingId).orElseThrow();

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            long amountInPaise = booking.getFinalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "txn_" + System.currentTimeMillis());

            JSONObject notes = new JSONObject();
            notes.put("userId", bookingId);
            notes.put("expectedTotal", amountInPaise);

            options.put("notes", notes);

            Order order = client.orders.create(options);

            String razorpayOrderId = order.get("id").toString();
            Long amount = ((Number) order.get("amount")).longValue();
            String currency = order.get("currency").toString();

            Payment payment = new Payment();

            payment.setBookingId(bookingId);
            payment.setAmount(booking.getFinalAmount());
            payment.setCurrency(currency);
            payment.setStatus("CREATED");
            payment.setProvider("RAZORPAY");
            payment.setProviderOrderId(razorpayOrderId);
            payment.setCreatedDate(LocalDateTime.now());

            paymentRepo.save(payment);

            return new RazorpayOrderResponseDTO(
                    keyId,
                    razorpayOrderId,
                    amount,
                    currency
            );
        } catch (Exception e) {
            throw new RuntimeException("Error creating Razorpay order", e);
        }
    }

    // VERIFY PAYMENT
    public void verifyPayment(Long bookingId, RazorpayVerifyPaymentRequestDTO dto)
            throws RazorpayException {

        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        if (paymentRepo.existsByProviderPaymentId(dto.getRazorpayPaymentId())) {
            return;
        }

        Booking booking = bookingRepo.findById(bookingId).orElseThrow();

        com.razorpay.Payment razorpayPayment =
                razorpay.payments.fetch(dto.getRazorpayPaymentId());

        String orderId = razorpayPayment.get("order_id").toString();

        if (!orderId.equals(dto.getRazorpayOrderId())) {
            throw new BadRequestException(
                    "Razorpay payment does not belong to the provided booking"
            );
        }

        String paymentStatus = razorpayPayment.get("status");

        if (!"captured".equalsIgnoreCase(paymentStatus)) {
            throw new BadRequestException("Payment is not captured yet");
        }

        String payload =
                dto.getRazorpayOrderId() + "|" + dto.getRazorpayPaymentId();

        String expectedSignature = hmacSHA256(payload, keySecret);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                dto.getRazorpaySignature().getBytes(StandardCharsets.UTF_8)
        )) {
            throw new BadRequestException(
                    "Razorpay signature verification failed"
            );
        }

        long amount = Long.parseLong(
                razorpayPayment.get("amount").toString()
        );

        Payment payment = paymentRepo
                .findByProviderOrderId(dto.getRazorpayOrderId())
                .orElseThrow();

        long expectedAmount = booking.getFinalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        if (expectedAmount != amount) {
            throw new RuntimeException("Amount mismatch");
        }

        payment.setProviderPaymentId(dto.getRazorpayPaymentId());
        payment.setProviderSignature(dto.getRazorpaySignature());
        payment.setStatus("SUCCESS");
        payment.setUpdatedDate(LocalDateTime.now());

        paymentRepo.save(payment);

        bookingService.confirmBooking(bookingId);
    }

    // REFUND
    public void refundPayment(Long bookingId, String reason) {
        try {
            Booking booking = bookingRepo.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            Payment payment = paymentRepo.findByBookingId(bookingId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
                throw new RuntimeException("Payment not successful");
            }

            BigDecimal refundAmount = calculateRefundAmount(payment);
            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("No refund applicable as per policy");
            }

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("payment_id", payment.getProviderPaymentId());

            BigDecimal amountInPaiseDecimal = refundAmount
                    .multiply(new BigDecimal("100"));

            long amountInPaise = amountInPaiseDecimal
                    .setScale(0, RoundingMode.DOWN)
                    .longValueExact();

            options.put("amount", amountInPaise);

            Refund refund = client.payments.refund(options);

            String providerRefundId = refund.get("id").toString();
            String razorpayStatus = refund.get("status").toString();

            BigDecimal razorpayAmount = new BigDecimal(
                    refund.get("amount").toString()
            ).divide(new BigDecimal("100"));

            String finalStatus;
            if ("processed".equalsIgnoreCase(razorpayStatus)) {
                finalStatus = "SUCCESS";
            } else if ("pending".equalsIgnoreCase(razorpayStatus)) {
                finalStatus = "PENDING";
            } else {
                finalStatus = "FAILED";
            }

            PaymentRefund pr = new PaymentRefund();
            pr.setPaymentId(payment.getPaymentId());
            pr.setRefundAmount(razorpayAmount);
            pr.setReason(reason);
            pr.setProviderRefundId(providerRefundId);
            pr.setStatus(finalStatus);
            pr.setCreatedDate(LocalDateTime.now());

            refundRepo.save(pr);

            payment.setStatus("REFUNDED");
            payment.setUpdatedDate(LocalDateTime.now());
            paymentRepo.save(payment);

            booking.setStatus("CANCELLED");
            booking.setUpdatedDate(LocalDateTime.now());
            bookingRepo.save(booking);
        } catch (Exception e) {
            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
        }
    }

    // WEBHOOK
    public void handleWebhook(String payload, String signature) {
        JSONObject event = new JSONObject(payload);
        String eventType = event.getString("event");

        if ("payment.captured".equals(eventType)) {
            JSONObject paymentEntity = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");

            Payment payment = paymentRepo
                    .findByProviderOrderId(orderId)
                    .orElseThrow();

            if ("SUCCESS".equals(payment.getStatus())) {
                return;
            }

            payment.setProviderPaymentId(paymentId);
            payment.setStatus("SUCCESS");
            payment.setUpdatedDate(LocalDateTime.now());

            paymentRepo.save(payment);

            bookingService.confirmBooking(payment.getBookingId());

            System.out.println("Payment updated via webhook");
        }
    }

    // HMAC SHA256
    private String hmacSHA256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(), "HmacSHA256"));

            byte[] hash = mac.doFinal(data.getBytes());
            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Signature generation failed", e);
        }
    }

    // REFUND POLICY
    private BigDecimal calculateRefundAmount(Payment payment) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bookingTime = payment.getCreatedDate();

        if (bookingTime == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long diffMinutes = java.time.Duration
                .between(bookingTime, now)
                .toMinutes();

        if (diffMinutes <= 10) {
            return payment.getAmount()
                    .setScale(2, RoundingMode.HALF_UP);
        }

        if (diffMinutes <= 60) {
            return payment.getAmount()
                    .multiply(new BigDecimal("0.5"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    // NEW: REFUND PREVIEW (used by BookingService)
    public Map<String, String> getRefundPreview(Payment payment) {
        BigDecimal refundAmount = calculateRefundAmount(payment);

        String tier;

        if (refundAmount.compareTo(payment.getAmount()) == 0) {
            tier = "FULL";
        } else if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            tier = "PARTIAL";
        } else {
            tier = "NONE";
        }

        Map<String, String> result = new HashMap<>();
        result.put("refundAmount", refundAmount.toPlainString());
        result.put("refundTier", tier);

        return result;
    }

    //    // REFUND
//    public void refundPayment(Long bookingId, String reason) {
//        try {
//            // Fetch booking & payment
//            Booking booking = bookingRepo.findById(bookingId)
//                    .orElseThrow(() -> new RuntimeException("Booking not found"));
//            Payment payment = paymentRepo.findByBookingId(bookingId)
//                    .orElseThrow(() -> new RuntimeException("Payment not found"));
//            if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
//                throw new RuntimeException("Payment not successful");
//            }
//
//            // Calculate refund amount (used only for request to Razorpay)
//            BigDecimal refundAmount = calculateRefundAmount(payment);
//            //BigDecimal refundAmount = BigDecimal.valueOf(100.00); // For testing, replace with above line in production
//            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
//                throw new RuntimeException("No refund applicable as per policy");
//            }
//
//            // Create Razorpay client
//            RazorpayClient client = new RazorpayClient(keyId, keySecret);
//
//            // Prepare request
//            JSONObject options = new JSONObject();
//            options.put("payment_id", payment.getProviderPaymentId());
//            options.put(
//                    "amount",
//                    refundAmount.multiply(BigDecimal.valueOf(100)).longValue()
//            );
//
//            // Call Razorpay Refund API
//            Refund refund = client.payments.refund(options);
//
//            // Extract data from Razorpay response
//            String providerRefundId = refund.get("id").toString();
//            String razorpayStatus = refund.get("status").toString();
//            BigDecimal razorpayAmount = new BigDecimal(refund.get("amount").toString())
//                    .divide(BigDecimal.valueOf(100));
//
//            // Map status
//            String finalStatus;
//            if ("processed".equalsIgnoreCase(razorpayStatus)) {
//                finalStatus = "SUCCESS";
//            } else if ("pending".equalsIgnoreCase(razorpayStatus)) {
//                finalStatus = "PENDING";
//            } else {
//                finalStatus = "FAILED";
//            }
//
//            // Save refund (SOURCE OF TRUTH = RAZORPAY)
//            PaymentRefund pr = new PaymentRefund();
//            pr.setPaymentId(payment.getPaymentId());
//            pr.setRefundAmount(razorpayAmount);
//            pr.setReason(reason);
//            pr.setProviderRefundId(providerRefundId);
//            pr.setStatus(finalStatus);
//            pr.setCreatedDate(LocalDateTime.now());
//            refundRepo.save(pr);
//
//            // Update payment
//            payment.setStatus("REFUNDED");
//            payment.setUpdatedDate(LocalDateTime.now());
//            paymentRepo.save(payment);
//
//            // Update booking
//            booking.setStatus("CANCELLED");
//            booking.setUpdatedDate(LocalDateTime.now());
//            bookingRepo.save(booking);
//        } catch (Exception e) {
//            throw new RuntimeException("Refund failed: " + e.getMessage(), e);
//        }
//    }

    //    // REFUND POLICY
//    private BigDecimal calculateRefundAmount(Payment payment) {
//        LocalDateTime now = LocalDateTime.now();
//        LocalDateTime bookingTime = payment.getCreatedDate();
//
//        if (bookingTime == null) {
//            return BigDecimal.ZERO;
//        }
//
//        long diffMinutes = java.time.Duration
//                .between(bookingTime, now)
//                .toMinutes();
//
//        if (diffMinutes <= 10) {
//            return payment.getAmount();
//        } else if (diffMinutes <= 60) {
//            return payment.getAmount().multiply(BigDecimal.valueOf(0.5));
//        } else {
//            return BigDecimal.ZERO;
//        }
//    }
}