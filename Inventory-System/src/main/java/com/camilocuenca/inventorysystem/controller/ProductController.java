package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.product.ProductDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProductService;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.camilocuenca.inventorysystem.service.serviceimpl.InventoryWebSocketNotifier inventoryWebSocketNotifier;

    @Autowired
    public ProductController(ProductService productService, InventoryService inventoryService, SimpMessagingTemplate messagingTemplate, com.camilocuenca.inventorysystem.service.serviceimpl.InventoryWebSocketNotifier inventoryWebSocketNotifier) {
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.messagingTemplate = messagingTemplate;
        this.inventoryWebSocketNotifier = inventoryWebSocketNotifier;
    }


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDto dto) {
        try {
            ProductDto out = productService.createProduct(dto);
            // publicar evento websocket con datos del producto
            com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto msg = new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(out.getId(), "CREATED", Instant.now());
            msg.setSku(out.getSku()); msg.setName(out.getName()); msg.setUnit(out.getUnit());
            messagingTemplate.convertAndSend("/topic/products", msg);

            // notify inventory listeners (global) and per branch
            try {
                inventoryWebSocketNotifier.notifyInventoryUpdate(null, out.getId(), null);
                // notificar por cada branch que tenga inventario del producto
                Page<com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto> p = inventoryService.getProductInventoryInAllBranches(null, out.getId(), PageRequest.of(0, 1000));
                p.getContent().forEach(iv -> inventoryWebSocketNotifier.notifyInventoryUpdate(iv.getBranchId(), out.getId(), iv.getQuantity() != null ? iv.getQuantity().intValue() : null));
            } catch (Exception ignore) {}

            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> listProducts(Pageable pageable) {
        Page<ProductDto> page = productService.listProducts(pageable);
        // publicar un evento ligero que indica que se hizo una consulta (opcional)
        messagingTemplate.convertAndSend("/topic/products", new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(null, "QUERY", Instant.now()));
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable UUID id) {
        try {
            ProductDto dto = productService.getProductById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductDto dto) {
        try {
            ProductDto out = productService.updateProduct(id, dto);
            com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto msg = new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(out.getId(), "UPDATED", Instant.now());
            msg.setSku(out.getSku()); msg.setName(out.getName()); msg.setUnit(out.getUnit());
            messagingTemplate.convertAndSend("/topic/products", msg);
            try {
                inventoryWebSocketNotifier.notifyInventoryUpdate(null, out.getId(), null);
                Page<com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto> p = inventoryService.getProductInventoryInAllBranches(null, out.getId(), PageRequest.of(0, 1000));
                p.getContent().forEach(iv -> inventoryWebSocketNotifier.notifyInventoryUpdate(iv.getBranchId(), out.getId(), iv.getQuantity() != null ? iv.getQuantity().intValue() : null));
            } catch (Exception ignore) {}
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id) {
        try {
            productService.deleteProduct(id);
            com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto msg = new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(id, "DELETED", Instant.now());
            messagingTemplate.convertAndSend("/topic/products", msg);
            try { inventoryWebSocketNotifier.notifyInventoryUpdate(null, id, null);
                Page<com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto> p = inventoryService.getProductInventoryInAllBranches(null, id, PageRequest.of(0, 1000));
                p.getContent().forEach(iv -> inventoryWebSocketNotifier.notifyInventoryUpdate(iv.getBranchId(), id, iv.getQuantity() != null ? iv.getQuantity().intValue() : null));
            } catch (Exception ignore) {}
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> importCsv(@RequestPart("file") MultipartFile file) {
        try {
            productService.importProductsFromCsv(file);
            com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto msg = new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(null, "IMPORTED", Instant.now());
            messagingTemplate.convertAndSend("/topic/products", msg);
            try { inventoryWebSocketNotifier.notifyInventoryUpdate(null, null, null);
                // try to notify per branch for any created products - skipping heavy work here
            } catch (Exception ignore) {}
            return ResponseEntity.accepted().body("Import started");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/import/template")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getTemplate() {
        String t = productService.getCsvTemplate();
        return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(t);
    }

    @PutMapping("/{productId}/providers/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addProviderToProduct(@PathVariable UUID productId, @PathVariable UUID providerId) {
        try {
            ProductDto out = productService.addProviderToProduct(productId, providerId);
            com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto msg = new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(out.getId(), "PROVIDER_ADDED", Instant.now());
            msg.setSku(out.getSku()); msg.setName(out.getName()); msg.setUnit(out.getUnit());
            messagingTemplate.convertAndSend("/topic/products", msg);
            try { inventoryWebSocketNotifier.notifyInventoryUpdate(null, out.getId(), null);
                Page<com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto> p = inventoryService.getProductInventoryInAllBranches(null, out.getId(), PageRequest.of(0, 1000));
                p.getContent().forEach(iv -> inventoryWebSocketNotifier.notifyInventoryUpdate(iv.getBranchId(), out.getId(), iv.getQuantity() != null ? iv.getQuantity().intValue() : null));
            } catch (Exception ignore) {}
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{productId}/providers/{providerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeProviderFromProduct(@PathVariable UUID productId, @PathVariable UUID providerId) {
        try {
            ProductDto out = productService.removeProviderFromProduct(productId, providerId);
            com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto msg = new com.camilocuenca.inventorysystem.dto.websocket.ProductUpdateMessageDto(out.getId(), "PROVIDER_REMOVED", Instant.now());
            msg.setSku(out.getSku()); msg.setName(out.getName()); msg.setUnit(out.getUnit());
            messagingTemplate.convertAndSend("/topic/products", msg);
            try { inventoryWebSocketNotifier.notifyInventoryUpdate(null, out.getId(), null);
                Page<com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto> p = inventoryService.getProductInventoryInAllBranches(null, out.getId(), PageRequest.of(0, 1000));
                p.getContent().forEach(iv -> inventoryWebSocketNotifier.notifyInventoryUpdate(iv.getBranchId(), out.getId(), iv.getQuantity() != null ? iv.getQuantity().intValue() : null));
            } catch (Exception ignore) {}
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
