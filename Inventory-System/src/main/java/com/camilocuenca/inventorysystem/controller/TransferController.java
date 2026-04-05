package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.transfer.TransferDispatchDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferPrepareDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferReceiveDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferRequestDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferResponseDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferListDto;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.TransferService;
import com.camilocuenca.inventorysystem.Enums.TransferStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Endpoint para solicitar una transferencia. El usuario autenticado es el solicitante.
     * @param authentication
     * @param body
     * @param bindingResult
     * @return
     */
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

    /**
     * Endpoint para preparar una transferencia. Solo el solicitante o un administrador pueden preparar la transferencia.
     * @param authentication
     * @param id id de la transferencia a preparar
     * @param body
     * @param bindingResult
     * @return
     */
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

    /**
     * Endpoint para despachar una transferencia. Solo el solicitante o un administrador pueden despachar la transferencia.
     * @param authentication
     * @param id
     * @param body
     * @param bindingResult
     * @return
     */
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

    /**
     * Endpoint para recibir una transferencia. Solo el solicitante o un administrador pueden recibir la transferencia.
     * @param authentication
     * @param id
     * @param body
     * @param bindingResult
     * @return
     */
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

    /**
     * Lista transferencias entrantes activas para la sucursal del usuario (o para la branchId si es ADMIN).
     * Cualquier usuario autenticado puede consultar; la validación de permisos se hace en el service.
     */
    @GetMapping("/transfers/incoming")
    public ResponseEntity<?> getIncomingTransfers(Authentication authentication, @RequestParam(required = false) UUID branchId, @RequestParam(required = false) TransferStatus status, Pageable pageable) {
        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        try {
            Page<TransferListDto> page = transferService.incomingTransfers(requesterId, branchId, status, pageable);
            return ResponseEntity.ok(page);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    /**
     * Lista transferencias salientes activas para la sucursal del usuario (o para la branchId si es ADMIN).
     */
    @GetMapping("/transfers/outgoing")
    public ResponseEntity<?> getOutgoingTransfers(Authentication authentication, @RequestParam(required = false) UUID branchId, @RequestParam(required = false) TransferStatus status, Pageable pageable) {
        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        try {
            Page<TransferListDto> page = transferService.outgoingTransfers(requesterId, branchId, status, pageable);
            return ResponseEntity.ok(page);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    /**
     * Obtiene el detalle completo de una transferencia por su id.
     * @param authentication
     * @param id id de la transferencia
     * @return TransferResponseDto o error HTTP
     */
    @GetMapping("/transfers/{id}")
    public ResponseEntity<?> getTransferById(Authentication authentication, @PathVariable UUID id) {
        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        try {
            TransferResponseDto dto = transferService.getTransferDetail(requesterId, id);
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    /**
     * Endpoint para obtener el cumplimiento logístico (estimatedArrival vs receivedAt) de una transferencia.
     * Solo puede ser consultado por el manager de la sucursal origen (la validación está en el service).
     */
    @GetMapping("/transfers/{id}/compliance")
    public ResponseEntity<?> getTransferCompliance(Authentication authentication, @PathVariable UUID id) {
        UUID requesterId = resolveRequesterId(authentication);
        if (requesterId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        try {
            var dto = transferService.getLogisticsCompliance(requesterId, id);
            return ResponseEntity.ok(dto);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
