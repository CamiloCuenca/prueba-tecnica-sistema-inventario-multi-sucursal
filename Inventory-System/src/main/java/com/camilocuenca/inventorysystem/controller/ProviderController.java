package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto;
import com.camilocuenca.inventorysystem.dto.provider.ProviderDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProviderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private final ProviderService providerService;

    @Autowired
    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @PostMapping
    public ResponseEntity<?> createProvider(@Valid @RequestBody ProviderDto dto) {
        try {
            ProviderDto out = providerService.createProvider(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<ProviderDto>> listProviders(Pageable pageable) {
        Page<ProviderDto> page = providerService.listProviders(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProvider(@PathVariable UUID id) {
        try {
            ProviderDto dto = providerService.getProviderById(id);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProvider(@PathVariable UUID id, @Valid @RequestBody ProviderDto dto) {
        try {
            ProviderDto out = providerService.updateProvider(id, dto);
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProvider(@PathVariable UUID id) {
        try {
            providerService.deleteProvider(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<Page<ProductByProviderDto>> getProductsByProvider(@PathVariable UUID id, Pageable pageable) {
        Page<ProductByProviderDto> page = providerService.listProductsByProvider(id, pageable);
        return ResponseEntity.ok(page);
    }
}

