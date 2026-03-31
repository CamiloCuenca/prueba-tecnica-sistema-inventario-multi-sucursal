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

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones sobre compras.
 *
 * Rutas base: /api/purchases
 *
 * Comportamiento general:
 * - Extrae el userId desde el `Authentication` mediante `AuthUtil.getUserId`.
 * - Verifica permisos donde aplica (por ejemplo creación en sucursal vía `AuthUtil.requireCreatePurchasePermission`).
 */

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;


    /**
     * Crea una nueva compra.
     * @param authentication credenciales del usuario (usadas para obtener userId y verificar permisos).
     * @param dto `PurchaseCreateDto` con los datos de la compra (incluye `branchId`).
     * @return ResponseEntity<PurchaseResponseDto>` con status 201 y la compra creada.
     */
    @PostMapping
    public ResponseEntity<PurchaseResponseDto> createPurchase(Authentication authentication, @RequestBody PurchaseCreateDto dto) {
        // extraer userId desde las credenciales del token
        UUID userId = AuthUtil.getUserId(authentication);
        // validar permisos: admin/manager pueden crear en cualquier sucursal; operator solo en su sucursal
        AuthUtil.requireCreatePurchasePermission(authentication, dto.getBranchId());

        PurchaseResponseDto created = purchaseService.createPurchase(userId, dto);
        return ResponseEntity.status(201).body(created);
    }


    /**Obtiene una compra por su id.
     *
     * @param authentication authentication: credenciales del usuario.
     * @param id UUID de la compra a recuperar.
     * @return `ResponseEntity<PurchaseResponseDto>` con la compra solicitada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponseDto> getPurchase(Authentication authentication, @PathVariable UUID id) {
        UUID userId = AuthUtil.getUserId(authentication);
        PurchaseResponseDto p = purchaseService.getPurchase(userId, id);
        return ResponseEntity.ok(p);
    }



    /** Registra la recepción de una compra (marcar como recibida / actualizar datos de recepción).
     *
     * @param authentication  authentication: credenciales del usuario.
     * @param id id: UUID de la compra a recibir.
     * @param dto PurchaseReceiveDto` con información de la recepción.
     * @return `ResponseEntity<PurchaseResponseDto>` con la compra actualizada tras la recepción.
     */
    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponseDto> receivePurchase(Authentication authentication, @PathVariable UUID id, @RequestBody PurchaseReceiveDto dto) {
        UUID userId = AuthUtil.getUserId(authentication);
        PurchaseResponseDto p = purchaseService.receivePurchase(userId, id, dto);
        return ResponseEntity.ok(p);
    }


    /**Lista compras.
     *
     * @param authentication credenciales del usuario.
     * @param branchId (opcional): si se especifica, filtra por sucursal.
     * @param pageable parámetros de paginación (actualmente no utilizados en todas las ramas)
     * @return `ResponseEntity<?>` con una lista de `PurchaseResponseDto`.
     */
    @GetMapping
    public ResponseEntity<?> listPurchases(Authentication authentication,
                                                                 @RequestParam(required = false) UUID branchId,
                                                                 Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        if (branchId != null) {
            List<PurchaseResponseDto> purchases = purchaseService.getPurchasesByBranch(userId, branchId);
            return ResponseEntity.ok(purchases);
        } else {
            // si no se especifica branchId, devolver all purchases (sólo permitido a admin/manager)
            List<PurchaseResponseDto> purchases = purchaseService.getAllPurchases(userId);
            return ResponseEntity.ok(purchases);
        }
    }


    /** Obtiene las compras de una sucursal específica.
     *
     * @param authentication credenciales del usuario.
     * @param branchId UUID de la sucursal.
     * @return ResponseEntity<List<PurchaseResponseDto>>` con las compras de la sucursal.
     */
    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<PurchaseResponseDto>> getPurchasesByBranch(Authentication authentication, @PathVariable UUID branchId) {
        UUID userId = AuthUtil.getUserId(authentication);
        List<PurchaseResponseDto> purchases = purchaseService.getPurchasesByBranch(userId, branchId);
        return ResponseEntity.ok(purchases);
    }




    /**
     * Obtiene todas las compras del sistema.
     * @param authentication authentication: credenciales del usuario.
     *
     * Permisos:
     * Sólo admin/manager deberían poder acceder (la verificación se realiza en el servicio).
     *
     * @return `ResponseEntity<List<PurchaseResponseDto>>` con todas las compras.
     */
    @GetMapping("/all")
    public ResponseEntity<List<PurchaseResponseDto>> getAllPurchases(Authentication authentication) {
        UUID userId = AuthUtil.getUserId(authentication);
        List<PurchaseResponseDto> purchases = purchaseService.getAllPurchases(userId);
        return ResponseEntity.ok(purchases);
    }
}
