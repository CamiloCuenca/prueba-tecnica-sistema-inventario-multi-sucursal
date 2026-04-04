package com.camilocuenca.inventorysystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Inventory System API")
                        .description("API REST para gestión de inventario multi-sucursal")
                        .version("v0.0.1")
                        .contact(new Contact().name("Juan Camilo Cuenca Sepulveda").email("camilocuencadev@gmail.com"))

                );
    }
}

