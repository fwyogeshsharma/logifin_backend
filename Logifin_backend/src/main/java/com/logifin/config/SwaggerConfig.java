package com.logifin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI logifinOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Logifin API")
                .description("Production-grade REST API for Logifin Backend Application. " +
                        "This API provides endpoints for user authentication, user management, " +
                        "role management, and password reset functionality.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Logifin Support")
                        .email("support@logifin.com")
                        .url("https://logifin.com"))
                .license(new License()
                        .name("Private License")
                        .url("https://logifin.com/license"));
    }

    private List<Server> servers() {
        Server localServer = new Server()
                .url(serverUrl)
                .description("Current Server");

        Server devServer = new Server()
                .url("http://localhost:8080")
                .description("Development Server");

        return Arrays.asList(localServer, devServer);
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("Enter JWT Bearer token **_only_**");
    }
}
