package com.camilocuenca.inventorysystem.dto.metrics;

import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de request para filtros del servicio de volumen de ventas.
 */
public class SalesVolumeRequestDto {

    private UUID branchId;
    private UUID productId;

    @PastOrPresent
    private LocalDate from;

    @PastOrPresent
    private LocalDate to;

    public SalesVolumeRequestDto() {
    }

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
