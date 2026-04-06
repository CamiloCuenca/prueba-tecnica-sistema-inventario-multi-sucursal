package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.product.ProductDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductDto dto) {
        try {
            ProductDto out = productService.createProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(out);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<ProductDto>> listProducts(Pageable pageable) {
        Page<ProductDto> page = productService.listProducts(pageable);
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
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
