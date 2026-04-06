package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.AuditLogDto;
import com.camilocuenca.inventorysystem.model.AuditLog;
import com.camilocuenca.inventorysystem.repository.AuditLogRepository;
import com.camilocuenca.inventorysystem.specification.AuditLogSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Controller para consultar los registros de auditoría.
 */
@RestController
@RequestMapping("/audit-log")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        var spec = AuditLogSpecifications.filter(usuario, accion, resultado, desde, hasta);
        Page<AuditLog> page = auditLogRepository.findAll(spec, pageable);
        Page<AuditLogDto> dtoPage = page.map(a -> new AuditLogDto(
                a.getId(), a.getAccion(), a.getUsuario(), a.getDescripcion(), a.getResultado(), a.getErrorMensaje(), a.getFecha()
        ));
        return ResponseEntity.ok(dtoPage);
    }
}

