package com.tradesoncall.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("TradesOnCall API")
                        .version("1.0.0")
                        .description("REST API for TradesOnCall - On-demand tradesperson marketplace. " +
                                "This API uses JWT (JSON Web Token) for authentication. " +
                                "To authenticate, include the JWT token in the Authorization header: " +
                                "Authorization: Bearer <your-access-token>. " +
                                "Getting Started: 1) Register a new user using /api/v1/users/register, " +
                                "2) Login using /api/v1/auth/login to get your access and refresh tokens, " +
                                "3) Use the access token in the Authorization header for protected endpoints, " +
                                "4) Refresh your token using /api/v1/auth/refresh when the access token expires.")
                        .contact(new Contact()
                                .name("TradesOnCall Team")
                                .email("api@tradesoncall.com")
                                .url("https://tradesoncall.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.tradesoncall.com")
                                .description("Production Server")
                ))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Authentication. Enter your token in the format: Bearer <token>")));
    }
}