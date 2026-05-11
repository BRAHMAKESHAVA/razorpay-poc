package org.backend.security;

import lombok.RequiredArgsConstructor;
import org.backend.security.handler.CustomAuthenticationEntryPoint;
import org.backend.security.handler.CustomeAccessDenaidHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final CustomeAccessDenaidHandler customeAccessDenaidHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;
    /**
     * This method defines the security filter chain for the application.
     * It configures various aspects of security, such as disabling CSRF protection,
     * setting session management to stateless, defining authorization rules for different endpoints,
     * adding a custom JWT authentication filter, and configuring exception handling for access denied
     * and authentication entry point scenarios.
     *
     * @param httpSecurity The HttpSecurity object used to configure security settings.
     * @return A SecurityFilterChain object that represents the configured security filter chain.
     * @throws Exception If an error occurs while configuring the security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrfConfig -> csrfConfig.disable())
                .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
                .sessionManagement(sessionConfig -> sessionConfig
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/user/register").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/auth/login/**").permitAll()
                        .requestMatchers("/user/**","/api/customer-address/**").hasAnyRole("CUSTOMER","ADMIN", "CAPTAIN", "PARTNER")
                        .anyRequest().authenticated() //authenticated()permitAll()denyAll
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customeAccessDenaidHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
        return httpSecurity.build();
    }

    /**
     * This method defines a bean for the PasswordEncoder, which is used to encode passwords in the application.
     * It returns an instance of BCryptPasswordEncoder, which is a widely used implementation of the PasswordEncoder interface.
     *
     * @return A PasswordEncoder object that can be used to encode passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * This method defines a bean for the CorsConfigurationSource, which is used to configure Cross-Origin Resource Sharing (CORS) settings for the application.
     * It creates a CorsConfiguration object and sets allowed origins, methods, headers, credentials, and max age based on the provided configuration.
     * The configuration is then registered with a UrlBasedCorsConfigurationSource to apply the CORS settings to all endpoints ("/**").
     *
     * @return A CorsConfigurationSource object that provides CORS configuration for the application.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Collections.singletonList("*"));        //config.setAllowedOriginPatterns(allowedOrigins);
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
