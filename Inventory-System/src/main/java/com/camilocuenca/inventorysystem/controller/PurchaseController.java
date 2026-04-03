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

    @PostMapping
    public ResponseEntity<PurchaseResponseDto> createPurchase(Authentication authentication, @RequestBody PurchaseCreateDto dto) {
        UUID userId = AuthUtil.getUserId(authentication);
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

    @GetMapping
    public ResponseEntity<Page<PurchaseSummaryDto>> listPurchases(Authentication authentication,
                                                                 @RequestParam(required = false) UUID branchId,
                                                                 @RequestParam(required = false) String status,
                                                                 Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        Page<PurchaseSummaryDto> page = purchaseService.listPurchases(userId, branchId, status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<Page<PurchaseSummaryDto>> getPurchasesByBranch(Authentication authentication,
                                                                         @PathVariable UUID branchId,
                                                                         @RequestParam(required = false) String status,
                                                                         Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        Page<PurchaseSummaryDto> page = purchaseService.listPurchases(userId, branchId, status, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<PurchaseSummaryDto>> getAllPurchases(Authentication authentication, @RequestParam(required = false) String status, Pageable pageable) {
        UUID userId = AuthUtil.getUserId(authentication);
        Page<PurchaseSummaryDto> page = purchaseService.listPurchases(userId, null, status, pageable);
        return ResponseEntity.ok(page);
    }
}
