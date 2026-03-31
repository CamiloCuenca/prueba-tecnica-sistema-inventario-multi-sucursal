package com.camilocuenca.inventorysystem.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PurchaseReceiveItemDto {
    private UUID purchaseDetailId;
    private BigDecimal quantityReceived;
}

