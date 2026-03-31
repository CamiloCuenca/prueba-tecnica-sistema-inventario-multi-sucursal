package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.purchase.PurchaseCreateDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseReceiveDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseResponseDto;

import java.util.UUID;

public interface PurchaseService {

    /**
     * Crea una nueva compra en el sistema.
     * @param requesterUserId
     * @param dto
     * @return
     */
    PurchaseResponseDto createPurchase(UUID requesterUserId, PurchaseCreateDto dto);

    /**
     * Marca una compra como recibida, actualizando su estado y el stock de los productos involucrados.
     * @param requesterUserId
     * @param purchaseId
     * @param dto
     * @return
     */
    PurchaseResponseDto receivePurchase(UUID requesterUserId, UUID purchaseId, PurchaseReceiveDto dto);

    /**
     * Obtiene los detalles de una compra específica.
     * @param requesterUserId
     * @param purchaseId
     * @return
     */
    PurchaseResponseDto getPurchase(UUID requesterUserId, UUID purchaseId);
}

