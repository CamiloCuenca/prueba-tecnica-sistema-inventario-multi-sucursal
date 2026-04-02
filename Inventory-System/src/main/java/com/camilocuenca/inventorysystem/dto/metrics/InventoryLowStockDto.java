package com.camilocuenca.inventorysystem.dto.metrics;

import java.math.BigDecimal;
import java.util.UUID;

public class InventoryLowStockDto {
    private UUID productId;
    private String productName;
    private BigDecimal currentStock;
    private BigDecimal minStock;

    public InventoryLowStockDto() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(BigDecimal currentStock) {
        this.currentStock = currentStock;
    }

    public BigDecimal getMinStock() {
        return minStock;
    }

    public void setMinStock(BigDecimal minStock) {
        this.minStock = minStock;
    }
}

