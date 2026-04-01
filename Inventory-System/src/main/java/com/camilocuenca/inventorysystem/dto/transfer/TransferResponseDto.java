package com.camilocuenca.inventorysystem.dto.transfer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TransferResponseDto {

    private UUID id;
    private UUID originBranchId;
    private UUID destinationBranchId;
    private String status;
    private Instant createdAt;
    private Instant shippedAt;
    private Instant receivedAt;
    private List<TransferDetailResponseDto> items;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOriginBranchId() {
        return originBranchId;
    }

    public void setOriginBranchId(UUID originBranchId) {
        this.originBranchId = originBranchId;
    }

    public UUID getDestinationBranchId() {
        return destinationBranchId;
    }

    public void setDestinationBranchId(UUID destinationBranchId) {
        this.destinationBranchId = destinationBranchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Instant shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public List<TransferDetailResponseDto> getItems() {
        return items;
    }

    public void setItems(List<TransferDetailResponseDto> items) {
        this.items = items;
    }
}
