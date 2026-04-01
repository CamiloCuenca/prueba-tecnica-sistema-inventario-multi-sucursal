package com.camilocuenca.inventorysystem.dto.sale;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class SaleItemRequestDto {

    @NotNull(message = "productId es requerido")
    private UUID productId;

    @NotNull(message = "quantity es requerida")
    @DecimalMin(value = "0.01", message = "quantity debe ser mayor que 0")
    private BigDecimal quantity;

    // precio unitario opcional (override)
    private BigDecimal price;

    // descuento absoluto en moneda por ítem (opcional)
    private BigDecimal discount;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}

