package com.camilocuenca.inventorysystem.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryViewDto {
    private UUID branchId;
    private String branchName;
    private UUID productId;
    private String sku;
    private String productName;
    private BigDecimal quantity;
    private Instant updatedAt;
}

