package com.camilocuenca.inventorysystem.dto.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SaleRequestDto {

    @NotNull(message = "branchId es requerido")
    private UUID branchId;

    @NotEmpty(message = "items no puede estar vacío")
    @Valid
    private List<SaleItemRequestDto> items;

    private BigDecimal discountTotal;

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public List<SaleItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequestDto> items) {
        this.items = items;
    }

    public BigDecimal getDiscountTotal() {
        return discountTotal;
    }

    public void setDiscountTotal(BigDecimal discountTotal) {
        this.discountTotal = discountTotal;
    }
}
