package com.camilocuenca.inventorysystem.dto.product;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;
import java.math.BigDecimal;

@Data
public class ProductByProviderDto {
    @NotNull
    private UUID productId;
    private String name;
    private String sku;
    private String unit;
}

