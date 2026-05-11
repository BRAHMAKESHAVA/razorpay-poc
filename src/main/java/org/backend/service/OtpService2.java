//package org.backend.service;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.backend.dto.OtpResponseDTO;
//import org.backend.dto.OtpVerifyRequestDTO;
//import org.backend.dto.OtpVerifyResponseDTO;
//import org.backend.dto.PartialOtpSendRequestDTO;
//import org.backend.enums.Role;
//import org.backend.exception.BadRequestException;
//import org.backend.exception.OtpException;
//import org.backend.model.Customer;
//import org.backend.model.Users;
//import org.backend.repository.CustomerRepository;
//import org.backend.repository.PartnerRepository;
//import org.backend.repository.UserRepository;
//import org.backend.utill.JwtUtill;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//
//import java.security.SecureRandom;
//import java.time.Duration;
//
///**
// * Service class for handling OTP (One-Time Password) operations.
// * Manages OTP generation, sending via SMS, validation, and rate limiting using Redis.
// * Provides authentication flow with JWT token generation upon successful OTP verification.
// */
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class OtpService {
//
//    private static final int OTP_EXPIRY_MINUTES = 2;
//    private static final int MAX_ATTEMPTS = 3;
//    private static final int LOCK_DURATION_MINUTES  = 10;
//    private static final int ATTEMPT_EXPIRY_MINUTES = 5;
//
//
//    private final StringRedisTemplate redisTemplate;
//    private final UserRepository userRepository;
//    private final AuthService authService;
//    private final JwtUtill jwtUtill;
//    private final SmsService smsService;
//    private final CustomerRepository customerRepository;
//    private final PartnerRepository partnerRepository;
//
//
//    private final SecureRandom random = new SecureRandom();
//
//    /**
//     * Generates and sends an OTP to the user's mobile number. It also implements rate limiting to prevent abuse.
//     * The OTP is stored in Redis with an expiration time, and the number of attempts is tracked to enforce limits.
//     * The method checks if the user exists based on the provided mobile number and role before generating the OTP.
//     * If the user is a customer, it also verifies that a corresponding customer record exists. If the OTP generation is successful,
//     * it sends the OTP via SMS and returns a response indicating success and the OTP expiry time.
//     * @param request
//     * @return
//     */
//    public OtpResponseDTO generateOtp(PartialOtpSendRequestDTO request) {
//        String mobile = request.getMobile();
//        String requestKey = "otp:req:" + mobile;
//        String lockKey = "otp:lock:" + mobile;
//        Long count = redisTemplate.opsForValue().increment(requestKey);
//
//        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
//            throw new OtpException("LOCKED",
//                    "You are temporarily blocked.",
//                    HttpStatus.TOO_MANY_REQUESTS
//            );
//        }
//
//        if (count == 1) redisTemplate.expire(requestKey, Duration.ofMinutes(ATTEMPT_EXPIRY_MINUTES ));
//
//        if (count > MAX_ATTEMPTS) {
//            redisTemplate.opsForValue().set(lockKey, "LOCKED", Duration.ofMinutes(LOCK_DURATION_MINUTES));
//            throw new OtpException(
//                    "TOO_MANY_REQUESTS",
//                    "Too many OTP requests. Please try again later.",
//                    HttpStatus.TOO_MANY_REQUESTS
//            );
//        }
//        //String otp = String.format("%04d", random.nextInt(10000));
//        String otp = "1234"; //static OTP
//
//        //smsService.sendOtp(mobile, otp);
//
//        redisTemplate.opsForValue().set("otp:code:" + mobile, otp, Duration.ofMinutes(OTP_EXPIRY_MINUTES));
//        redisTemplate.delete("otp:attempts:" + mobile);
//        return OtpResponseDTO.builder()
//                //.message("OTP: " + otp)
//                .message("OTP sent successfully to " + mobile)
//                .expiryMinutes(OTP_EXPIRY_MINUTES)
//                .build();
//    }
//
//    /**
//     * Validates the OTP provided by the user. It checks if the OTP exists and matches the one stored in Redis for the given mobile number.
//     * The method also tracks the number of validation attempts and enforces limits to prevent brute-force attacks. If the OTP is valid, it deletes the OTP and attempt records from Redis.
//     * It then checks the user's role and ensures that a corresponding customer record exists for customers.
//     * Finally, it generates and returns access and refresh tokens for the authenticated user.
//     * @param otpVerifyRequest
//     * @param request
//     * @return
//     */
//    public OtpVerifyResponseDTO validateOtp(OtpVerifyRequestDTO otpVerifyRequest, HttpServletRequest request) {
//        String mobile = otpVerifyRequest.getMobile();
//        Role role = otpVerifyRequest.getRole();
//
//        String userOtp = otpVerifyRequest.getOtp();
//        String otpKey = "otp:code:" + mobile;
//        String requestKey = "otp:req:" + mobile;
//        String attemptsKey = "otp:attempts:" + mobile;
//        String lockKey = "otp:lock:" + mobile;
//
//        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
//            throw new OtpException("LOCKED", "Your are temporarily blocked.",HttpStatus.TOO_MANY_REQUESTS);
//        }
//
//        String storedOtp = redisTemplate.opsForValue().get(otpKey);
//        if (storedOtp == null) {
//            throw new OtpException("EXPIRED", "OTP expired. Request a new one.", HttpStatus.GONE);
//        }
//
//        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
//        if (attempts == 1) {
//            redisTemplate.expire(attemptsKey, Duration.ofMinutes(ATTEMPT_EXPIRY_MINUTES));
//        }
//
//        if (attempts > MAX_ATTEMPTS) {
//            redisTemplate.opsForValue().set(lockKey, "LOCKED", Duration.ofMinutes(LOCK_DURATION_MINUTES));
//            throw new OtpException("MAX_ATTEMPTS",
//                    "Too many incorrect attempts. Please request a new OTP.",
//                    HttpStatus.TOO_MANY_REQUESTS);
//        }
//
//        if (!storedOtp.equals(userOtp)) {
//            throw new OtpException("INVALID", "Invalid OTP. Please try again.", HttpStatus.BAD_REQUEST);
//        }
//        redisTemplate.delete(otpKey);
//        redisTemplate.delete(attemptsKey);
//        redisTemplate.delete(requestKey);
//
////        if (otpVerifyRequest.getRole() == Role.PARTNER) {
////            boolean partnerExists = partnerRepository.existsByMobile(otpVerifyRequest.getMobile());
////            if (!partnerExists) {
////                throw new BadRequestException("Only our onboard partners can register from this platform.");
////            }
////        }
//
//        // STEP 1: Find or create USER
//        Users user = userRepository.findByMobile(mobile)
//                .orElseGet(() -> {
//                    Users newUser = Users.builder()
//                            .mobile(mobile)
//                            .role(role)
//                            .build();
//                    return userRepository.save(newUser);
//                });
//
//        Customer customer = null;
//
//        if (role == Role.CUSTOMER) {
//            customer = customerRepository.findByUsers(user)
//                    .orElseGet(() ->
//                            customerRepository.save(
//                                    Customer.builder().users(user).build()
//                            )
//                    );
//        }
//
//        String source = authService.extractSource(request);
//        return OtpVerifyResponseDTO.builder()
//                .userId(user.getId())
//                .customerId(customer != null ? customer.getCustomerId() : null)
//                .isProfileComplete(user.isProfileComplete())
//                .accessToken(jwtUtill.generateAccessToken(user, source))
//                .refreshToken(jwtUtill.generateRefreshToken(user, source))
//                .build();
//    }
//}
