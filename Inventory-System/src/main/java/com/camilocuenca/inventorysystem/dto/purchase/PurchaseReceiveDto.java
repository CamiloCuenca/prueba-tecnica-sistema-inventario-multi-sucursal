package com.camilocuenca.inventorysystem.dto.purchase;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class PurchaseReceiveDto {
    private List<PurchaseReceiveItemDto> items;
    private Instant receivedAt;
}

