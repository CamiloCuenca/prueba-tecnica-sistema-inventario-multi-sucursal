package com.camilocuenca.inventorysystem.dto.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalePriceUpdateDto {
    @NotNull(message = "salePrice es requerido")
    private BigDecimal salePrice;
}

