package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.sale.SaleRequestDto;
import com.camilocuenca.inventorysystem.dto.sale.SaleResponseDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.SaleService;
import com.camilocuenca.inventorysystem.service.serviceInterface.ReceiptService;
import com.camilocuenca.inventorysystem.service.serviceimpl.PdfGenerator;
import com.camilocuenca.inventorysystem.exceptions.InsufficientStockException;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SaleController {

    private final SaleService saleService;
    private final UserRepository userRepository;
    private final PdfGenerator pdfGenerator;
    private final ReceiptService receiptService;

    @Autowired
    public SaleController(SaleService saleService, UserRepository userRepository, PdfGenerator pdfGenerator, ReceiptService receiptService) {
        this.saleService = saleService;
        this.userRepository = userRepository;
        this.pdfGenerator = pdfGenerator;
        this.receiptService = receiptService;
    }

    private UUID resolveRequesterId(Authentication authentication) {
        if (authentication == null) return null;
        Object creds = authentication.getCredentials();
        if (creds != null) {
            try {
                return UUID.fromString(String.valueOf(creds));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (authentication.getPrincipal() == null) return null;
        String principal = String.valueOf(authentication.getPrincipal());
        Optional<User> user = userRepository.findByEmail(principal);
        return user.map(User::getId).orElse(null);
    }

    @PostMapping("/sales")
    public ResponseEntity<?> createSale(Authentication authentication, @Valid @RequestBody SaleRequestDto body, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }

        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }

        try {
            SaleResponseDto resp = saleService.createSale(body, requesterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (InsufficientStockException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage()));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse("ERROR", ex.getReason()));
        }
    }

    @GetMapping("/sales/{saleId}/receipt")
    public ResponseEntity<?> getSaleReceipt(@PathVariable UUID saleId) {
        byte[] pdf = pdfGenerator.generateSaleReceipt(saleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "receipt-" + saleId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @GetMapping("/sales/{saleId}/receipt/view")
    public ResponseEntity<?> viewSaleReceipt(@PathVariable UUID saleId) {
        byte[] pdf = receiptService.getSaleReceiptPdf(saleId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "receipt-" + saleId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @Getter
    static class ErrorResponse {
        private final String code;
        private final String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

    }
}
