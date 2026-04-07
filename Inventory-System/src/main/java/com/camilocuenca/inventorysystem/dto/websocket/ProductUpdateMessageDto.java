package com.camilocuenca.inventorysystem.dto.websocket;

import java.util.UUID;
import java.time.Instant;

public class ProductUpdateMessageDto {
    private UUID productId;
    private String event; // CREATED, UPDATED, DELETED, IMPORTED
    private Instant timestamp;

    // optional product info to avoid extra GET calls from UI
    private String sku;
    private String name;
    private String unit;

    public ProductUpdateMessageDto() {}

    public ProductUpdateMessageDto(UUID productId, String event, Instant timestamp) {
        this.productId = productId;
        this.event = event;
        this.timestamp = timestamp;
    }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
