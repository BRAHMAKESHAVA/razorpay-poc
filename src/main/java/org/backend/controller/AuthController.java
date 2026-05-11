package org.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.dto.*;
import org.backend.dto.auth.request.GenerateTokenRequest;
import org.backend.dto.auth.request.SendOtpRequest;
import org.backend.dto.auth.request.VerifyOtpRequest;
import org.backend.dto.auth.response.AuthResponseDTO;
import org.backend.dto.auth.response.RefreshTokenResponseDTO;
import org.backend.dto.auth.response.SendOtpResponse;
import org.backend.dto.common.ApiResponse;
import org.backend.exception.BadRequestException;
import org.backend.service.AuthService;
import org.backend.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication and authorization operations.
 *
 * This controller handles OTP-based authentication flows including OTP generation,
 * verification, and JWT token refresh operations. All endpoints are accessible at
 * the base path "/auth/login".
 *
 * The authentication flow typically follows:
 * 1. Client sends phone number to receive OTP via {@link #sendOtp(OtpSendRequestDTO)}
 * 2. Client verifies OTP to receive JWT tokens via {@link #verifyOtp(OtpVerifyRequestDTO, HttpServletRequest)}
 * 3. Client refreshes expired access tokens via {@link #refreshToken(String, HttpServletRequest)}
 *
 * @author Stylo User Management Service
 * @version 1.0
 */
@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    /** Service for OTP generation and validation operations */
    private final OtpService otpService;

    /** Service for JWT token generation and refresh operations */
    private final AuthService authService;

    /**
     * Generates and sends an OTP to the provided phone number.
     *
     * This endpoint initiates the authentication process by sending a One-Time Password
     * to the user's registered phone number via SMS or other communication channel.
     *
     * @param request the OTP send request containing the phone number
     * @return ResponseEntity containing the OTP response with status and message
     * @throws jakarta.validation.ConstraintViolationException if the request body validation fails
     */
    @PostMapping("/sendOTP")
    public ResponseEntity<SendOtpResponse> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        log.info(String.valueOf(request));
        return ResponseEntity.ok(otpService.generateOtp(request));
    }

    /**
     * Verifies the OTP provided by the user and issues JWT tokens upon successful verification.
     *
     * This endpoint validates the OTP that was previously sent to the user's phone number.
     * Upon successful verification, it generates and returns JWT access and refresh tokens
     * for authenticated session management.
     *
     * @param request the OTP verification request containing the phone number and OTP code
     * @param httpRequest the HTTP servlet request containing request metadata and headers
     * @return ResponseEntity containing the OTP verification response with JWT tokens
     * @throws jakarta.validation.ConstraintViolationException if the request body validation fails
     */
    @PostMapping("/verifyOTP")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        AuthResponseDTO  response = otpService.validateOtp(request, httpRequest);
        return ResponseEntity.ok(
                ApiResponse.<AuthResponseDTO >builder()
                        .status(true)
                        .message("OTP verified successfully")
                        .data(response)
                        .build()
        );
    }

    /**
     * Refreshes an expired JWT access token using a valid refresh token.
     *
     * This endpoint allows clients to obtain a new access token without requiring
     * re-authentication. The refresh token is extracted from the Authorization header
     * (Bearer token format) and used to issue a new access token.
     *
     * @param authorizationHeader the Authorization header containing the refresh token in Bearer format
     * @param httpRequest the HTTP servlet request containing request metadata and headers
     * @return ResponseEntity containing the token refresh response with the new access token
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader, HttpServletRequest httpRequest) {
        String refreshToken = authorizationHeader.replace("Bearer ", "").trim();
        RefreshTokenResponseDTO response = authService.refreshToken(refreshToken, httpRequest);
        return ResponseEntity.ok(
                ApiResponse.<RefreshTokenResponseDTO>builder()
                        .status(true)
                        .message("Token refreshed successfully")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/token")
    public ResponseEntity<ApiResponse<AuthResponseDTO >> generateToken(
            @Valid @RequestBody GenerateTokenRequest request, HttpServletRequest httpRequest) {
        String mobileNumber = request.getMobile();
        AuthResponseDTO  response = authService.generateToken(mobileNumber, httpRequest);

        return ResponseEntity.ok(
                ApiResponse.<AuthResponseDTO >builder()
                        .status(true)
                        .message("Token generated successfully")
                        .data(response)
                        .build()
        );
    }

}
