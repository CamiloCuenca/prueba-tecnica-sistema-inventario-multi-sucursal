package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.purchase.PurchaseCreateDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseReceiveDto;
import com.camilocuenca.inventorysystem.dto.purchase.PurchaseResponseDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.PurchaseService;
import com.camilocuenca.inventorysystem.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    @Autowired
    private PurchaseService purchaseService;

    @PostMapping
    public ResponseEntity<PurchaseResponseDto> createPurchase(Authentication authentication, @RequestBody PurchaseCreateDto dto) {
        // extraer userId desde las credenciales del token
        UUID userId = AuthUtil.getUserId(authentication);
        // validar permisos: admin/manager pueden crear en cualquier sucursal; operator solo en su sucursal
        AuthUtil.requireCreatePurchasePermission(authentication, dto.getBranchId());

        PurchaseResponseDto created = purchaseService.createPurchase(userId, dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseResponseDto> getPurchase(Authentication authentication, @PathVariable UUID id) {
        UUID userId = AuthUtil.getUserId(authentication);
        PurchaseResponseDto p = purchaseService.getPurchase(userId, id);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<PurchaseResponseDto> receivePurchase(Authentication authentication, @PathVariable UUID id, @RequestBody PurchaseReceiveDto dto) {
        UUID userId = AuthUtil.getUserId(authentication);
        PurchaseResponseDto p = purchaseService.receivePurchase(userId, id, dto);
        return ResponseEntity.ok(p);
    }
}
