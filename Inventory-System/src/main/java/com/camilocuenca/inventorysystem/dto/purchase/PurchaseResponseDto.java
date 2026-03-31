package com.camilocuenca.inventorysystem.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PurchaseResponseDto {
    private UUID id;
    private UUID branchId;
    private String supplier;
    private PurchaseStatusDto status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal discountTotal;
    private BigDecimal total;
    private Instant createdAt;
    private List<PurchaseDetailResponseDto> details;

    @Getter
    @Setter
    public static class PurchaseStatusDto {
        private String name;
    }
}

