package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class TransferPrepareItemDto {

    @NotNull(message = "productId es requerido")
    private UUID productId;

    @NotNull(message = "quantityConfirmed es requerido")
    @Positive(message = "quantityConfirmed debe ser mayor que cero")
    private BigDecimal quantityConfirmed;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public BigDecimal getQuantityConfirmed() {
        return quantityConfirmed;
    }

    public void setQuantityConfirmed(BigDecimal quantityConfirmed) {
        this.quantityConfirmed = quantityConfirmed;
    }
}

