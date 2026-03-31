package com.camilocuenca.inventorysystem.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PurchaseDetailResponseDto {
    private UUID id;
    private UUID productId;
    private BigDecimal orderedQuantity;
    private BigDecimal receivedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal lineTotal;
}

