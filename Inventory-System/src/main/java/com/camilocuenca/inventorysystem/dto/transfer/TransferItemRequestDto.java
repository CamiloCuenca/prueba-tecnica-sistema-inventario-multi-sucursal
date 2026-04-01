package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferItemRequestDto {

    @NotNull(message = "productId es requerido")
    private UUID productId;

    @NotNull(message = "quantity es requerido")
    @Positive(message = "quantity debe ser mayor que cero")
    private BigDecimal quantity;

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

