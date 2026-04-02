package com.camilocuenca.inventorysystem.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class ProductPriceDto {
    private UUID id;

    @NotNull
    private UUID productId;

    @Size(max = 100)
    private String label;

    @NotNull
    private BigDecimal price;

    private String currency = "COP";

    private Instant effectiveFrom;
    private Instant effectiveTo;
}

