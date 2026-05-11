package org.backend.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.backend.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This method is invoked when an unauthenticated user attempts to access a protected resource.
     * It constructs an ErrorResponseDTO object containing details about the authentication error,
     * sets the HTTP status to 401 Unauthorized, and writes the error response as JSON to the HTTP response body.
     *
     * @param request The HttpServletRequest object containing details of the incoming request.
     * @param response The HttpServletResponse object for sending responses back to the client.
     * @param ex The AuthenticationException that triggered this entry point, providing details about the authentication failure.
     * @throws IOException If an I/O error occurs during response writing.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        log.warn("Unauthorized access attempt | URI: {} | Method: {} | Reason: {}",
                request.getRequestURI(),
                request.getMethod(),
                ex.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status_code(HttpStatus.UNAUTHORIZED.value())
                .status("UNAUTHORIZED")
                .message("Authentication token is required to access this resource")
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
