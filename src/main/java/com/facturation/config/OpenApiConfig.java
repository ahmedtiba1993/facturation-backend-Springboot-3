package com.facturation.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Ahmed Tiba",
                        email = "ahmed.tiba.1993@gmail.com",
                        url ="https://ahmedtiba1993.github.io/"
                ),
                title = "Facturation",
                version = "1.0.0",
                description = "Platforme pour créer et de gérer des factures"
        ),
        servers = {
                @Server(description = "Server localhost 127.0.0.1",url = "http://localhost:8080"),
                @Server(description = "Server AWS EC2",url = "13.38.17.45"),
                @Server(description = "DNS Server AWS EC2",url = "ec2-13-38-17-45.eu-west-3.compute.amazonaws.com"),
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenConfig() {

        return new OpenAPI()
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                ).components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",
                                        new SecurityScheme()
                                                .name("bearerAuth")
                                                .type(Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )
                ;
    }
}
