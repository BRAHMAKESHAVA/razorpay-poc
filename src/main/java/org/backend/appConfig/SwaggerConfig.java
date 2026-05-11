package org.backend.appConfig;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * This class sets up the OpenAPI specification for the Stylo Customer API,
 * including server information, security requirements, and authentication schemes.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Creates a custom OpenAPI bean for the application.
     * Configures the API title, description, local development server,
     * and JWT bearer authentication.
     *
     * @return the configured OpenAPI object with API info, servers, and security settings.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stylo Customer Swagger API")
                        .description("API documentation for Stylo Customer")
                )
                .servers(List.of(
                        new Server().url("http://localhost:9091").description("Local Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                        )
                );


    }
}
