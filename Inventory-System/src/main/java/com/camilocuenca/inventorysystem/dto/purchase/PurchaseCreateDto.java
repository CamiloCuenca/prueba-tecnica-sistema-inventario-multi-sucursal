package com.camilocuenca.inventorysystem.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PurchaseCreateDto {
    private UUID branchId;
    private UUID provider_id;
    private List<PurchaseDetailCreateDto> items;
    private String paymentTerms;
    private Instant expectedDeliveryDate;
    private String notes;
}

