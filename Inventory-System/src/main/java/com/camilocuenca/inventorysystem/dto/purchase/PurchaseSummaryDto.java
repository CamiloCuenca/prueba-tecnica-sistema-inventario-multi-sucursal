package com.camilocuenca.inventorysystem.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PurchaseSummaryDto {
    private UUID id;
    private UUID branchId;
    private String supplier;
    private String status;
    private BigDecimal total;
    private Instant createdAt;
}

