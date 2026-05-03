package com.example.project.gpa_calculator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GPA Calculator REST API")
                        .version("1.0.0")
                        .description("""
                                Secure GPA Calculator backend built with Spring Boot.
                                
                                Features:
                                - User registration and login
                                - JWT authentication
                                - Refresh token support
                                - Semester CRUD
                                - Subject/course management
                                - GPA and CGPA calculation
                                - User-specific protected data
                                """)
                        .contact(new Contact()
                                .name("Mohamed Abdul Shafi")
                                .email("mohamedsadik763@gmail.com"))
                        .license(new License()
                                .name("Educational Project")))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }
}