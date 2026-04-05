package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.branch.BranchDto;
import com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto;
import com.camilocuenca.inventorysystem.dto.inventory.ProductCatalogItemDto;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProductPriceService;
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
import com.camilocuenca.inventorysystem.dto.product.ProductPriceDto;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.camilocuenca.inventorysystem.dto.metrics.InventoryLowStockDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;
    private final ProductPriceService productPriceService;
    private final com.camilocuenca.inventorysystem.service.serviceimpl.LowStockNotifierService lowStockNotifierService;

    /**
     * Constructor para inyección de dependencias. Se inyecta InventoryService para manejar la lógica de inventario* @param inventoryService servicio de inventario
     * @param userRepository repositorio de usuarios
     */
    @Autowired
    public InventoryController(InventoryService inventoryService, UserRepository userRepository, ProductPriceService productPriceService, com.camilocuenca.inventorysystem.service.serviceimpl.LowStockNotifierService lowStockNotifierService) {
        this.inventoryService = inventoryService;
        this.userRepository = userRepository;
        this.productPriceService = productPriceService;
        this.lowStockNotifierService = lowStockNotifierService;
    }

    /**
     * Método auxiliar para resolver el ID del usuario autenticado a partir del objeto Authentication.
     * @param authentication objeto de autenticación
     * @return UUID del usuario autenticado o null
     */
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

    /**
     * Método auxiliar para resolver el ID de la sucursal del usuario autenticado a partir del objeto Authentication.
     * @param authentication objeto de autenticación
     * @return UUID de la sucursal del usuario autenticado o null
     */
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

    /**
     * Endpoint para obtener el catálogo de productos de la sucursal del usuario autenticado. Permite paginación, búsqueda por nombre y opción para mostrar solo productos con stock.
     * @param authentication objeto de autenticación
     * @param pageable paginación
     * @param q consulta opcional por nombre o sku
     * @param showEmpty si true muestra también productos sin stock
     * @return página con items del catálogo
     */
    @GetMapping("/my/catalog")
    public ResponseEntity<Page<ProductCatalogItemDto>> getMyCatalog(Authentication authentication,
                                                                     Pageable pageable,
                                                                     @RequestParam(required = false) String q,
                                                                     @RequestParam(defaultValue = "false") boolean showEmpty) {
        UUID requesterId = resolveRequesterId(authentication);
        Page<ProductCatalogItemDto> page = inventoryService.getOwnBranchCatalog(requesterId, pageable, q, showEmpty);
        return ResponseEntity.ok(page);
    }

    /**
     * Endpoint para obtener el inventario de una sucursal específica. Permite paginación y búsqueda por nombre. Solo usuarios con permisos adecuados pueden acceder a esta información.
     * @param authentication objeto de autenticación
     * @param branchId id de la sucursal
     * @param pageable paginación
     * @param q filtro opcional por nombre/sku
     * @return página con inventario de la sucursal
     */
    @GetMapping("/branches/{branchId}/inventory")
    public ResponseEntity<Page<InventoryViewDto>> getBranchInventory(Authentication authentication,
                                                                      @PathVariable UUID branchId,
                                                                      Pageable pageable,
                                                                      @RequestParam(required = false) String q) {
        UUID requesterId = resolveRequesterId(authentication);
        Page<InventoryViewDto> page = inventoryService.getBranchInventory(requesterId, branchId, pageable, q);
        return ResponseEntity.ok(page);
    }

    /**
     * Endpoint para obtener el inventario de un producto específico en una sucursal. Solo usuarios con permisos adecuados pueden acceder a esta información.
     * @param authentication objeto de autenticación
     * @param branchId id de la sucursal
     * @param productId id del producto
     * @return detalle de inventario o 404 si no existe
     */
    @GetMapping("/branches/{branchId}/inventory/{productId}")
    public ResponseEntity<InventoryViewDto> getProductInBranch(Authentication authentication,
                                                               @PathVariable UUID branchId,
                                                               @PathVariable UUID productId) {
        UUID requesterId = resolveRequesterId(authentication);
        Optional<InventoryViewDto> dto = inventoryService.getProductInventoryInBranch(requesterId, branchId, productId);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Nuevo endpoint para obtener el inventario de un producto en todas las sucursales (paginado).
     * Permite a cualquier usuario autenticado ver la disponibilidad por sucursal.
     * @param authentication objeto de autenticación
     * @param productId id del producto
     * @param pageable paginación
     * @return página con inventario por sucursal
     */
    @GetMapping("/products/{productId}/inventory")
    public ResponseEntity<Page<InventoryViewDto>> getProductInAllBranches(Authentication authentication,
                                                                          @PathVariable UUID productId,
                                                                          Pageable pageable) {
        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Page.empty());
        }
        Page<InventoryViewDto> page = inventoryService.getProductInventoryInAllBranches(requesterId, productId, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Endpoint para actualizar el precio de venta de un producto en una sucursal. Solo usuarios con permisos adecuados pueden realizar esta acción. Se valida el cuerpo de la solicitud y se manejan los errores de validación.
     * @param authentication objeto de autenticación
     * @param branchId id de la sucursal
     * @param productId id del producto
     * @param body dto con nuevo precio
     * @param bindingResult resultado de validación
     * @return 204 en caso de éxito
     */
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
        boolean isManager = auths.stream().anyMatch(a -> "ROLE_MANAGER".equals(a.getAuthority()));
        boolean isOperator = auths.stream().anyMatch(a -> "ROLE_OPERATOR".equals(a.getAuthority()));

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

    /**
     * Endpoint para listar los nombres e IDs de todas las sucursales.
     * Cualquier usuario autenticado puede consultar esta lista.
     *
     * Nota: se mapea a `/api/branches/names` para no colisionar con el CRUD de `Branch` en `/api/branches`.
     */
    @GetMapping("/branches/names")
    public ResponseEntity<java.util.List<BranchDto>> getAllBranches(Authentication authentication) {
        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<BranchDto> branches = inventoryService.getAllBranches();
        return ResponseEntity.ok(branches);
    }

    /**
     * Endpoint para listar productos por proveedor (paginado).
     * Cualquier usuario autenticado puede consultar esta lista.
     */
    @GetMapping("/providers/{providerId}/products")
    public ResponseEntity<Page<com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto>> getProductsByProvider(Authentication authentication,
                                                                                                                        @PathVariable UUID providerId,
                                                                                                                        Pageable pageable) {
         UUID requesterId = resolveRequesterId(authentication);
         if (requesterId == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
         }
         Page<com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto> page = inventoryService.getProductsByProvider(requesterId, providerId, pageable);
         return ResponseEntity.ok(page);
     }

    /**
     * Listar precios de referencia para un producto.
     */
    @GetMapping("/products/{productId}/prices")
    public ResponseEntity<java.util.List<ProductPriceDto>> getPricesForProduct(@PathVariable UUID productId) {
        java.util.List<ProductPriceDto> list = productPriceService.getPricesForProduct(productId);
        return ResponseEntity.ok(list);
    }

    /**
     * Crear un precio de referencia para un producto. Requiere autenticación.
     */
    @PostMapping("/products/{productId}/prices")
    public ResponseEntity<ProductPriceDto> createPrice(@PathVariable UUID productId, @Valid @RequestBody ProductPriceDto body) {
        body.setProductId(productId);
        ProductPriceDto created = productPriceService.createPrice(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/prices/{priceId}")
    public ResponseEntity<ProductPriceDto> updatePrice(@PathVariable UUID priceId, @Valid @RequestBody ProductPriceDto body) {
        ProductPriceDto updated = productPriceService.updatePrice(priceId, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/prices/{priceId}")
    public ResponseEntity<Void> deletePrice(@PathVariable UUID priceId) {
        productPriceService.deletePrice(priceId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint para obtener alertas de stock bajo para una sucursal.
     * Requiere que el usuario tenga ROLE_MANAGER o ROLE_ADMIN.
     */
    @GetMapping("/inventory/low-stock-alerts")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<InventoryLowStockDto>> getLowStockAlerts(@RequestParam UUID branchId) {
        java.util.List<InventoryLowStockDto> list = inventoryService.getLowStockAlerts(branchId);
        return ResponseEntity.ok(list);
    }

    /**
     * Endpoint para disparar notificaciones manualmente sobre alertas de stock bajo.
     * Requiere que el usuario tenga ROLE_MANAGER o ROLE_ADMIN.
     */
    @PostMapping("/inventory/low-stock-alerts/notify")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<?> triggerLowStockNotifications(Authentication authentication, @RequestParam UUID branchId) {
        UUID userId = resolveRequesterId(authentication);
        lowStockNotifierService.notifyLowStock(branchId, userId);
        return ResponseEntity.accepted().body("Notifications triggered");
    }
}
