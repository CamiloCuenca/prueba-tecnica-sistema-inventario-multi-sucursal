package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class TransferPrepareDto {

    @NotEmpty(message = "items no puede ser vacío")
    @Valid
    private List<TransferPrepareItemDto> items;

    public List<TransferPrepareItemDto> getItems() {
        return items;
    }

    public void setItems(List<TransferPrepareItemDto> items) {
        this.items = items;
    }
}

