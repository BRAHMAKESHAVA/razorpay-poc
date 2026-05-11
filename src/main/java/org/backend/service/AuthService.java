package org.backend.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.backend.dto.auth.response.AuthResponseDTO;
import org.backend.dto.auth.response.RefreshTokenResponseDTO;
import org.backend.exception.ResourceNotFoundException;
import org.backend.model.Customer;
import org.backend.model.Users;
import org.backend.repository.CustomerRepository;
import org.backend.repository.UserRepository;
import org.backend.utill.JwtUtill;
import org.springframework.stereotype.Service;

/**
 * Service class for handling authentication-related operations.
 * Provides functionality for token refresh and request source extraction.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtill jwtUtill;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /**
     * Extracts the source (origin) of the request from the HttpServletRequest.
     * The method checks for the "Origin" header in the request and extracts the domain name by removing the "http://" or "https://" prefix.
     * If the "Origin" header is not present, it falls back to using the server name and port from the request.
     *
     * @param request The HttpServletRequest object containing details of the incoming request.
     * @return A string representing the source (origin) of the request.
     */
    String extractSource(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            return origin.replace("http://", "").replace("https://", "");
        }
        return request.getServerName() + ":" + request.getServerPort();
    }

    /**
     * Refreshes the access token and refresh token for a user based on the provided refresh token.
     * The method performs the following steps:
     * 1. Extracts the mobile number from the provided refresh token using the JwtUtill.
     * 2. Extracts the source (origin) of the request to ensure that tokens are generated for the correct client.
     * 3. Retrieves the user associated with the extracted mobile number from the database. If no user is found, a ResourceNotFoundException is thrown.
     * 4. Generates a new access token and refresh token for the user using the JwtUtill, passing in the user details and source.
     * 5. Returns a TokenRefreshResponseDTO containing the newly generated access token and refresh token.
     *
     * @param refreshToken The refresh token provided by the client for refreshing tokens.
     * @param request The HttpServletRequest object containing details of the incoming request.
     * @return A TokenRefreshResponseDTO containing the new access token and refresh token.
     * @throws ResourceNotFoundException If no user is found with the extracted mobile number from the refresh token.
     */
    public RefreshTokenResponseDTO refreshToken(String refreshToken, HttpServletRequest request) {

        boolean isGuest = jwtUtill.isGuestToken(refreshToken);
        String source = extractSource(request);
        String subject = jwtUtill.getSubject(refreshToken);

        if (!isGuest) {
            Long userId = Long.valueOf(subject);
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            return RefreshTokenResponseDTO.builder()
                    .accessToken(jwtUtill.generateAccessToken(user, source))
                    .refreshToken(refreshToken)
                    .build();

        }

        return RefreshTokenResponseDTO.builder()
                .accessToken(jwtUtill.generateAccessToken(null, source))
                .refreshToken(refreshToken)
                .build();

    }

    public AuthResponseDTO generateToken(String mobile, HttpServletRequest httpRequest) {
        Users user = userRepository.findByMobile(mobile).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Customer customer = customerRepository.findByUsers(user).orElse(null);
        String source = extractSource(httpRequest);

        String accessToken = jwtUtill.generateAccessToken(user, source);
        String refreshToken = jwtUtill.generateRefreshToken(user, source);

        return AuthResponseDTO .builder()
                .userId(user.getId())
                .customerId(customer != null ? customer.getCustomerId() : null)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
