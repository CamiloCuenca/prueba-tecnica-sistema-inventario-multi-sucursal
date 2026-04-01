package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class TransferRequestDto {

    @NotNull(message = "originBranchId es requerido")
    private UUID originBranchId;

    // destinationBranchId es opcional en el DTO: si el usuario es ADMIN debe proveerla;
    // si el usuario no es ADMIN, el servicio usará la sucursal del usuario autenticado.
    private UUID destinationBranchId;

    @NotEmpty(message = "items no puede ser vacío")
    @Valid
    private List<TransferItemRequestDto> items;

    public UUID getOriginBranchId() {
        return originBranchId;
    }

    public void setOriginBranchId(UUID originBranchId) {
        this.originBranchId = originBranchId;
    }

    public UUID getDestinationBranchId() {
        return destinationBranchId;
    }

    public void setDestinationBranchId(UUID destinationBranchId) {
        this.destinationBranchId = destinationBranchId;
    }

    public List<TransferItemRequestDto> getItems() {
        return items;
    }

    public void setItems(List<TransferItemRequestDto> items) {
        this.items = items;
    }
}
