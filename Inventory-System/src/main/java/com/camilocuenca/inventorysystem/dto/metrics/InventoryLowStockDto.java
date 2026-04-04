package com.camilocuenca.inventorysystem.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryLowStockDto {
    private UUID productId;
    private String productName;
    private String sku;
    private Integer currentStock;
    private Integer minStock;
    private Integer difference;
    private String category;
    private String supplier;
    private String urgencyLevel;
}

