package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.purchase.PurchaseCreateDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseReceiveDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseResponseDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseSummaryDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.PurchaseService;
import com.camilocuenca.inventorysystem.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para operaciones sobre compras.
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    /**
     * Crea una nueva compra. El usuario debe tener permiso para crear compras en la sucursal especificada.
     * @param authentication
     * @param dto
     * @return
     */
    @PostMapping
    public ResponseEntity<PurchaseResponseDto> createPurchase(Authentication authentication, @RequestBody PurchaseCreateDto dto) {
        UUID userId = AuthUtil.getUserId(authentication);
        AuthUtil.requireCreatePurchasePermission(authentication, dto.getBranchId());

        PurchaseResponseDto created = purchaseService.createPurchase(userId, dto);
        return ResponseEntity.status(201).body(created);
    }

    /**
     * Obtiene los detalles de una compra específica. El usuario debe tener permiso para ver compras en la sucursal asociada a la compra.
     * @param authentication
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponseDto> getPurchase(Authentication authentication, @PathVariable UUID id) {
        UUID userId = AuthUtil.getUserId(authentication);
        PurchaseResponseDto p = purchaseService.getPurchase(userId, id);
        return ResponseEntity.ok(p);
    }

    /**
     * Marca una compra como recibida. El usuario debe tener permiso para recibir compras en la sucursal asociada a la compra.
     * @param authentication
     * @param id
     * @param dto
     * @return
     */
    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponseDto> receivePurchase(Authentication authentication, @PathVariable UUID id, @RequestBody PurchaseReceiveDto dto) {
        UUID userId = AuthUtil.getUserId(authentication);
        PurchaseResponseDto p = purchaseService.receivePurchase(userId, id, dto);
        return ResponseEntity.ok(p);
    }

    /**
     * Lista las compras del usuario autenticado, con opciones de filtrado por sucursal y estado. El usuario solo verá las compras de las sucursales para las que tiene permiso de visualización.
     * @param authentication
     * @param branchId
     * @param status
     * @param pageable
     * @return
     */
    @GetMapping
    public ResponseEntity<Page<PurchaseSummaryDto>> listPurchases(Authentication authentication,
                                                                 @RequestParam(required = false) UUID branchId,
                                                                 @RequestParam(required = false) String status,
                                                                 Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        Page<PurchaseSummaryDto> page = purchaseService.listPurchases(userId, branchId, status, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista las compras de una sucursal específica. El usuario solo verá las compras de esta sucursal si tiene permiso de visualización para ella.
     * @param authentication
     * @param branchId
     * @param status
     * @param pageable
     * @return
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<Page<PurchaseSummaryDto>> getPurchasesByBranch(Authentication authentication,
                                                                         @PathVariable UUID branchId,
                                                                         @RequestParam(required = false) String status,
                                                                         Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        Page<PurchaseSummaryDto> page = purchaseService.listPurchases(userId, branchId, status, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Lista todas las compras del sistema. Solo los usuarios con permiso de visualización global podrán acceder a esta ruta.
     * @param authentication
     * @param status
     * @param pageable
     * @return
     */

    @GetMapping("/all")
    public ResponseEntity<Page<PurchaseSummaryDto>> getAllPurchases(Authentication authentication, @RequestParam(required = false) String status, Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        Page<PurchaseSummaryDto> page = purchaseService.listPurchases(userId, null, status, pageable);
        return ResponseEntity.ok(page);
    }
}
