package com.camilocuenca.inventorysystem.dto.transfer;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferDetailResponseDto {

    private UUID productId;
    private String productName;
    private BigDecimal quantityRequested;
    private BigDecimal quantityConfirmed;
    private BigDecimal receivedQuantity;

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

    public BigDecimal getQuantityRequested() {
        return quantityRequested;
    }

    public void setQuantityRequested(BigDecimal quantityRequested) {
        this.quantityRequested = quantityRequested;
    }

    public BigDecimal getQuantityConfirmed() {
        return quantityConfirmed;
    }

    public void setQuantityConfirmed(BigDecimal quantityConfirmed) {
        this.quantityConfirmed = quantityConfirmed;
    }

    public BigDecimal getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(BigDecimal receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
}

