package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.transfer.TransferDispatchDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferPrepareDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferReceiveDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferRequestDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferResponseDto;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    @Autowired
    public TransferController(TransferService transferService, UserRepository userRepository) {
        this.transferService = transferService;
        this.userRepository = userRepository;
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

    @PostMapping("/transfers")
    public ResponseEntity<?> requestTransfer(Authentication authentication, @Valid @RequestBody TransferRequestDto body, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }

        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");

        try {
            TransferResponseDto resp = transferService.requestTransfer(body, requesterId);
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @PostMapping("/transfers/{id}/prepare")
    public ResponseEntity<?> prepareTransfer(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody TransferPrepareDto body, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }

        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");

        try {
            TransferResponseDto resp = transferService.prepareTransfer(id, body, requesterId);
            return ResponseEntity.ok(resp);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PostMapping("/transfers/{id}/dispatch")
    public ResponseEntity<?> dispatchTransfer(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody TransferDispatchDto body, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }

        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");

        try {
            TransferResponseDto resp = transferService.dispatchTransfer(id, body, requesterId);
            return ResponseEntity.ok(resp);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PostMapping("/transfers/{id}/receive")
    public ResponseEntity<?> receiveTransfer(Authentication authentication, @PathVariable UUID id, @Valid @RequestBody TransferReceiveDto body, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldErrors());
        }

        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");

        try {
            TransferResponseDto resp = transferService.receiveTransfer(id, body, requesterId);
            return ResponseEntity.ok(resp);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}

