package org.backend.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.backend.dto.common.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;

@Component
@Slf4j
public class CustomeAccessDenaidHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This method is invoked when an authenticated user attempts to access a resource for which they do not have the necessary permissions.
     * It constructs an ErrorResponseDTO object containing details about the access denial,
     * sets the HTTP status to 403 Forbidden, and writes the error response as JSON to the HTTP response body.
     *
     * @param request The HttpServletRequest object containing details of the incoming request.
     * @param response The HttpServletResponse object for sending responses back to the client.
     * @param accessDeniedException The AccessDeniedException that triggered this handler, providing details about the access denial.
     * @throws IOException If an I/O error occurs during response writing.
     * @throws ServletException If an error occurs during servlet processing.
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.warn("Access denied | URI: {} | Method: {} | User: {} | Reason: {}",
                request.getRequestURI(),
                request.getMethod(),
                request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "ANONYMOUS",
                accessDeniedException.getMessage());

        ErrorResponseDTO error = ErrorResponseDTO.builder()
                .status_code(HttpStatus.FORBIDDEN.value())
                .status("ACCESS_DENIED")
                .message("You don't have permission to access this resource")
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
