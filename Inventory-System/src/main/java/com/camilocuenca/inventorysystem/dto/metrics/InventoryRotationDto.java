package com.camilocuenca.inventorysystem.dto.metrics;

import java.math.BigDecimal;
import java.util.UUID;

public class InventoryRotationDto {
    private UUID productId;
    private String sku;
    private String productName;
    private BigDecimal currentStock;
    private BigDecimal totalSold;
    private Double rotationIndex;

    public InventoryRotationDto() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
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

    public BigDecimal getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(BigDecimal totalSold) {
        this.totalSold = totalSold;
    }

    public Double getRotationIndex() {
        return rotationIndex;
    }

    public void setRotationIndex(Double rotationIndex) {
        this.rotationIndex = rotationIndex;
    }
}

