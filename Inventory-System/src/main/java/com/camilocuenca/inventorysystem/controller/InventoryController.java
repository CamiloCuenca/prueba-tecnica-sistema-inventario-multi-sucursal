package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto;
import com.camilocuenca.inventorysystem.dto.inventory.ProductCatalogItemDto;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import com.camilocuenca.inventorysystem.dto.inventory.SalePriceUpdateDto;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    @Autowired
    public InventoryController(InventoryService inventoryService, UserRepository userRepository) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
    }

    private UUID resolveRequesterId(Authentication authentication) {
        if (authentication == null) return null;

        // Preferir el userId almacenado en credentials por el JwtAuthenticationFilter
        Object creds = authentication.getCredentials();
        if (creds != null) {
            try {
                return UUID.fromString(String.valueOf(creds));
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Fallback: buscar por email (subject/principal)
        if (authentication.getPrincipal() == null) return null;
        String principal = String.valueOf(authentication.getPrincipal());
        Optional<User> user = userRepository.findByEmail(principal);
        return user.map(User::getId).orElse(null);
    }

    private UUID resolveRequesterBranchId(Authentication authentication) {
        if (authentication == null) return null;
        Object details = authentication.getDetails();
        if (details != null) {
            try {
                return UUID.fromString(String.valueOf(details));
            } catch (IllegalArgumentException ignored) {
            }
        }
        // fallback: try to load user and get branch
        if (authentication.getPrincipal() == null) return null;
        String principal = String.valueOf(authentication.getPrincipal());
        Optional<User> user = userRepository.findByEmail(principal);
        return user.flatMap(u -> u.getBranch() != null ? Optional.of(u.getBranch().getId()) : Optional.empty()).orElse(null);
    }
    @GetMapping("/my/catalog")

    public ResponseEntity<Page<ProductCatalogItemDto>> getMyCatalog(Authentication authentication,
                                                                     Pageable pageable,
                                                                     @RequestParam(required = false) String q,
                                                                     @RequestParam(defaultValue = "false") boolean showEmpty) {
        UUID requesterId = resolveRequesterId(authentication);
        Page<ProductCatalogItemDto> page = inventoryService.getOwnBranchCatalog(requesterId, pageable, q, showEmpty);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/branches/{branchId}/inventory")
    public ResponseEntity<Page<InventoryViewDto>> getBranchInventory(Authentication authentication,
                                                                      @PathVariable UUID branchId,
                                                                      Pageable pageable,
                                                                      @RequestParam(required = false) String q) {
        UUID requesterId = resolveRequesterId(authentication);
        Page<InventoryViewDto> page = inventoryService.getBranchInventory(requesterId, branchId, pageable, q);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/branches/{branchId}/inventory/{productId}")
    public ResponseEntity<InventoryViewDto> getProductInBranch(Authentication authentication,
                                                               @PathVariable UUID branchId,
                                                               @PathVariable UUID productId) {
        UUID requesterId = resolveRequesterId(authentication);
        Optional<InventoryViewDto> dto = inventoryService.getProductInventoryInBranch(requesterId, branchId, productId);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/branches/{branchId}/inventory/{productId}/sale-price")
    public ResponseEntity<?> updateSalePrice(Authentication authentication,
                                             @PathVariable UUID branchId,
                                             @PathVariable UUID productId,
                                             @Valid @RequestBody SalePriceUpdateDto body,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }

        // Resolve requester info
        UUID requesterId = resolveRequesterId(authentication);
        UUID requesterBranchId = resolveRequesterBranchId(authentication);
        if (requesterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        // Check authorities
        Collection<? extends GrantedAuthority> auths = authentication.getAuthorities();
        boolean isAdmin = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isManager = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
        boolean isOperator = auths.stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERATOR"));

        if (isOperator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Operadores no pueden actualizar precios");
        }

        if (isManager) {
            if (requesterBranchId == null || !requesterBranchId.equals(branchId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Managers solo pueden actualizar su propia sucursal");
            }
        }

        // isAdmin can update any branch

        // Call service
        inventoryService.updateSalePrice(productId, branchId, body.getSalePrice());
        return ResponseEntity.noContent().build();
    }
}
