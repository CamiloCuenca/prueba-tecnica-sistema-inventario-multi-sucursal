package com.camilocuenca.inventorysystem.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCatalogItemDto {
    private UUID productId;
    private String sku;
    private String name;
    private String unit;
    private BigDecimal quantity;
}
