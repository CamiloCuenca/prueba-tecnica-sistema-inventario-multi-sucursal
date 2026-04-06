package com.camilocuenca.inventorysystem.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ProductDto {
    private UUID id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 100)
    private String sku;

    private String unit;

    // Lista de provider ids asociados al producto
    private Set<UUID> providerIds;
}

