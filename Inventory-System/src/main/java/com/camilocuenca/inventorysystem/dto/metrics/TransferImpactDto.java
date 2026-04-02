package com.camilocuenca.inventorysystem.dto.metrics;

import java.util.List;
import java.util.UUID;

public class TransferImpactDto {
    private UUID transferId;
    private UUID originBranchId;
    private String originBranchName;
    private UUID destinationBranchId;
    private String destinationBranchName;
    private String status;
    private List<TransferImpactItemDto> items;

    public TransferImpactDto() {
    }

    public UUID getTransferId() {
        return transferId;
    }

    public void setTransferId(UUID transferId) {
        this.transferId = transferId;
    }

    public UUID getOriginBranchId() {
        return originBranchId;
    }

    public void setOriginBranchId(UUID originBranchId) {
        this.originBranchId = originBranchId;
    }

    public String getOriginBranchName() {
        return originBranchName;
    }

    public void setOriginBranchName(String originBranchName) {
        this.originBranchName = originBranchName;
    }

    public UUID getDestinationBranchId() {
        return destinationBranchId;
    }

    public void setDestinationBranchId(UUID destinationBranchId) {
        this.destinationBranchId = destinationBranchId;
    }

    public String getDestinationBranchName() {
        return destinationBranchName;
    }

    public void setDestinationBranchName(String destinationBranchName) {
        this.destinationBranchName = destinationBranchName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TransferImpactItemDto> getItems() {
        return items;
    }

    public void setItems(List<TransferImpactItemDto> items) {
        this.items = items;
    }
}

