package com.camilocuenca.inventorysystem.dto.logistics;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferImpactItemDto {
    private UUID productId;
    private BigDecimal quantity;

    public TransferImpactItemDto() {
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }
}

