package com.camilocuenca.inventorysystem.dto.websocket;

import java.util.UUID;
import java.time.Instant;

public class InventoryUpdateMessageDto {
    private UUID branchId;
    private UUID productId;
    private Integer currentStock;
    private Instant timestamp;

    public InventoryUpdateMessageDto() {}

    public InventoryUpdateMessageDto(UUID branchId, UUID productId, Integer currentStock, Instant timestamp) {
        this.branchId = branchId;
        this.productId = productId;
        this.currentStock = currentStock;
        this.timestamp = timestamp;
    }

    public UUID getBranchId() { return branchId; }
    public void setBranchId(UUID branchId) { this.branchId = branchId; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

