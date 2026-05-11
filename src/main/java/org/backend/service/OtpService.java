package org.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.dto.auth.request.SendOtpRequest;
import org.backend.dto.auth.request.VerifyOtpRequest;
import org.backend.dto.auth.response.AuthResponseDTO;
import org.backend.dto.auth.response.SendOtpResponse;
import org.backend.enums.Role;
import org.backend.exception.OtpException;
import org.backend.model.Customer;
import org.backend.model.Users;
import org.backend.repository.CustomerRepository;
import org.backend.repository.UserRepository;
import org.backend.utill.JwtUtill;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * Service class for handling OTP (One-Time Password) operations.
 * Manages OTP generation, sending via SMS, validation, and rate limiting using Redis.
 * Provides authentication flow with JWT token generation upon successful OTP verification.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 3;
    //private static final int OTP_EXPIRY_SECONDS = 30;
    private static final int MAX_ATTEMPTS = 3;
    private static final int OTP_LOCK_DURATION_MINUTES = 10;

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final JwtUtill jwtUtill;
    private final SmsService smsService;
    private final CustomerRepository customerRepository;

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates and sends an OTP to the user's mobile number. It also implements rate limiting to prevent abuse.
     * The OTP is stored in Redis with an expiration time, and the number of attempts is tracked to enforce limits.
     * The method checks if the user exists based on the provided mobile number and role before generating the OTP.
     * If the user is a customer, it also verifies that a corresponding customer record exists. If the OTP generation is successful,
     * it sends the OTP via SMS and returns a response indicating success and the OTP expiry time.
     * @param request
     * @return
     */
    // GENERATE OTP
    public SendOtpResponse generateOtp(SendOtpRequest request) {
        String mobile = request.getMobile();
        Role role = request.getRole();

        //        if (!userRepository.existsByMobile(mobile)) {
        //            String roleName = role.name().toLowerCase();
        //            roleName = Character.toUpperCase(roleName.charAt(0)) + roleName.substring(1);
        //            throw new ResourceNotFoundException(roleName + " is not registered.");
        //        }

        String requestKey = "otp:req:" + mobile;
        Long count = redisTemplate.opsForValue().increment(requestKey);

        if (count == 1)
            redisTemplate.expire(
                    requestKey,
                    Duration.ofMinutes(OTP_LOCK_DURATION_MINUTES)
                    //requestKey, Duration.ofSeconds(OTP_EXPIRY_SECONDS)
            );

        if (count > MAX_ATTEMPTS) {
            throw new OtpException(
                    "TOO_MANY_REQUESTS",
                    "Too many OTP requests. Please try again later.",
                    HttpStatus.TOO_MANY_REQUESTS
            );
        }

        //String otp = String.format("%04d", random.nextInt(10000));
        String otp = "1234"; //static OTP

        //smsService.sendOtp(mobile, otp);

        redisTemplate.opsForValue().set(
                "otp:code:" + mobile,
                otp,
                Duration.ofMinutes(OTP_EXPIRY_MINUTES)
        );

        redisTemplate.delete("otp:attempts:" + mobile);

        return SendOtpResponse.builder()
                .message("OTP: " + otp)
                //.message("OTP sent successfully to " + mobile)
                .expiryMinutes(OTP_EXPIRY_MINUTES)
                .build();
    }

    /**
     * Validates the OTP provided by the user. It checks if the OTP exists and matches the one stored in Redis for the given mobile number.
     * The method also tracks the number of validation attempts and enforces limits to prevent brute-force attacks. If the OTP is valid, it deletes the OTP and attempt records from Redis.
     * It then checks the user's role and ensures that a corresponding customer record exists for customers.
     * Finally, it generates and returns access and refresh tokens for the authenticated user.
     * @param otpVerifyRequest
     * @param request
     * @return
     */
    // VALIDATE OTP
    public AuthResponseDTO validateOtp(VerifyOtpRequest otpVerifyRequest, HttpServletRequest request) {
        String mobile = otpVerifyRequest.getMobile();
        Role role = otpVerifyRequest.getRole();
        String userOtp = otpVerifyRequest.getOtp();

        //String roleName = role.name().toLowerCase();
        //String formattedRoleName  = Character.toUpperCase(roleName.charAt(0)) + roleName.substring(1);
        //Users user = userRepository.findByMobile(mobile).orElseThrow(() -> new ResourceNotFoundException(formattedRoleName +" not found with the provided mobile number."));

        String otpKey = "otp:code:" + mobile;
        String attemptsKey = "otp:attempts:" + mobile;
        String storedOtp = redisTemplate.opsForValue().get(otpKey);

        if (storedOtp == null) {
            throw new OtpException("EXPIRED", "OTP expired. Request a new one.", HttpStatus.GONE);
        }

        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(5));
        }

        if (attempts > MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey);
            throw new OtpException("MAX_ATTEMPTS",
                    "Too many attempts. Please request a new OTP.",
                    HttpStatus.TOO_MANY_REQUESTS);
        }

        if (!storedOtp.equals(userOtp)) {
            throw new OtpException("INVALID", "Invalid OTP. Please try again.", HttpStatus.BAD_REQUEST);
        }

        Users user = userRepository.findByMobile(mobile).orElse(null);
        String source = authService.extractSource(request);

        Long userId = null;
        Long customerId = null;

        if (user != null) {
            userId = user.getId();

            if (role == Role.CUSTOMER) {
                Customer customer = customerRepository.findByUsers(user)
                        .orElseGet(() ->
                                customerRepository.save(
                                        Customer.builder().users(user).build()
                                )
                        );

                customerId = customer.getCustomerId();
            }
        }

        String accessToken = jwtUtill.generateAccessToken(user, source);
        String refreshToken = jwtUtill.generateRefreshToken(user, source);

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);
        //redisTemplate.opsForValue().set("verified:mobile:" + mobile, mobile);

        return AuthResponseDTO.builder()
                .userId(userId)
                .customerId(customerId)
                .accessToken(accessToken)
                .isProfileComplete(user != null)
                .refreshToken(refreshToken)
                .build();
    }
}