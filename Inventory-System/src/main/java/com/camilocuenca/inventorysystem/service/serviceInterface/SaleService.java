package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.sale.SaleRequestDto;
import com.camilocuenca.inventorysystem.dto.sale.SaleResponseDto;

import java.util.UUID;

/**
 * Interfaz para el servicio de ventas (módulo Sale).
 *
 * Esta interfaz define el contrato para las operaciones relacionadas con
 * la creación y consulta de ventas.
 */
public interface SaleService {
    /**
     * Crea una venta con sus detalles validando stock y registrando transacciones de inventario.
     * @param req DTO con la información de la venta
     * @param requesterUserId UUID del usuario que solicita la operación (resuelto desde JWT)
     * @return DTO con la venta creada
     */
    SaleResponseDto createSale(SaleRequestDto req, UUID requesterUserId);
}
