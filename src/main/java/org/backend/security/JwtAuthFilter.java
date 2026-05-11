package org.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.repository.UserRepository;
import org.backend.utill.JwtUtill;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JwtUtill jwtUtill;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final CustomeUserDetailsService customeUserDetailsService;

    /**
     * This method determines whether the filter should be applied to a given HTTP request.
     * It checks if the request method is "OPTIONS" or if the request path starts with "/auth/login" or "/user/register".
     * If any of these conditions are true, the filter will not be applied to the request,
     * allowing it to proceed without JWT authentication. This is typically done to allow unauthenticated access to login and registration endpoints, as well as to handle preflight requests for CORS.
     * @param request The HttpServletRequest object containing details of the incoming request.
     * @return A boolean value indicating whether the filter should not be applied to the request (true) or should be applied (false).
     */

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase("OPTIONS") ||
                request.getServletPath().startsWith("/auth/login") ||
                request.getServletPath().startsWith("/user/register");
    }

    /**
     * This method is responsible for filtering incoming HTTP requests and performing JWT authentication.
     * It checks for the presence of a JWT token in the "Authorization" header of the request, validates it, and sets the authentication context if the token is valid.
     * If the token is missing or invalid, it allows the request to proceed without authentication.
     *
     * @param request The HttpServletRequest object containing details of the incoming request.
     * @param response The HttpServletResponse object for sending responses back to the client.
     * @param filterChain The FilterChain object that allows the request to proceed through the filter chain.
     * @throws ServletException If an error occurs during servlet processing.
     * @throws IOException If an I/O error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String requestTokenHeader = request.getHeader("Authorization");
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = requestTokenHeader.substring(7);

            jwtUtill.validateToken(token);
            String subject = jwtUtill.getSubject(token);
            boolean isGuest = jwtUtill.isGuestToken(token);

            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (isGuest) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    subject, // GUEST
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    UserDetails userDetails = customeUserDetailsService.loadUserByUsername(subject);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception ex){
            log.error("JWT Authentication failed: {}", ex.getMessage());
            handlerExceptionResolver.resolveException(request,response,null,ex);
        }
    }
}
