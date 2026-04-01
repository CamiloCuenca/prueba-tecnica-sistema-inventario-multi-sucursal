package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class TransferReceiveDto {

    @NotEmpty(message = "items no puede ser vacío")
    @Valid
    private List<TransferReceiveItemDto> items;

    public List<TransferReceiveItemDto> getItems() {
        return items;
    }

    public void setItems(List<TransferReceiveItemDto> items) {
        this.items = items;
    }

    public static class TransferReceiveItemDto {
        @NotNull(message = "productId es requerido")
        private UUID productId;

        @NotNull(message = "receivedQuantity es requerido")
        private BigDecimal receivedQuantity;

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public BigDecimal getReceivedQuantity() {
            return receivedQuantity;
        }

        public void setReceivedQuantity(BigDecimal receivedQuantity) {
            this.receivedQuantity = receivedQuantity;
        }
    }
}

