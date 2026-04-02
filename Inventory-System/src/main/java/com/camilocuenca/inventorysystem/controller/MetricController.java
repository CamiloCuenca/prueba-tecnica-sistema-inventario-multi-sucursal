package com.camilocuenca.inventorysystem.controller;

import com.camilocuenca.inventorysystem.dto.metrics.BranchPerformanceDto;
import com.camilocuenca.inventorysystem.dto.metrics.InventoryLowStockDto;
import com.camilocuenca.inventorysystem.dto.metrics.InventoryBehaviorDto;
import com.camilocuenca.inventorysystem.dto.metrics.SalesVolumeDto;
import com.camilocuenca.inventorysystem.dto.metrics.TransferImpactDto;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import com.camilocuenca.inventorysystem.service.serviceInterface.metricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/metrics")
public class MetricController {

    private final metricService metricService;
    private final InventoryService inventoryService;

    @Autowired
    public MetricController(metricService metricService, InventoryService inventoryService) {
        this.metricService = metricService;
        this.inventoryService = inventoryService;
    }

    /**
     * Volumen de ventas del mes en curso vs meses anteriores.
     * Acceso: cualquier usuario autenticado.
     *
     * Ejemplo: GET /api/metrics/sales-volume?branchId={uuid}&from=2026-01-01&to=2026-03-31&page=0&size=20
     */
    @GetMapping("/sales-volume")
    public ResponseEntity<Page<SalesVolumeDto>> salesVolume(
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        Page<SalesVolumeDto> page = metricService.salesVolume(branchId, productId, from, to, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Alias que permite pasar branchId como path variable: /api/metrics/sales-volume/{branchId}
     * Conserva el resto de query params (productId, from, to, pageable).
     */
    @GetMapping("/sales-volume/{branchId}")
    public ResponseEntity<Page<SalesVolumeDto>> salesVolumeByBranch(
            @PathVariable UUID branchId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        Page<SalesVolumeDto> page = metricService.salesVolume(branchId, productId, from, to, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * Endpoint que devuelve el comportamiento del inventario filtrado por la sucursal del usuario.
     * No permite pasar branchId por query para respetar la autonomía operativa (si se necesita, solo admin podrá).
     */
    @GetMapping("/inventory-behavior")
    public ResponseEntity<InventoryBehaviorDto> inventoryBehavior() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID branchId = null;
        if (auth != null && auth.getDetails() != null) {
            try {
                branchId = UUID.fromString(String.valueOf(auth.getDetails()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (branchId == null) {
            return ResponseEntity.badRequest().build();
        }
        InventoryBehaviorDto resp = metricService.getInventoryBehavior(branchId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Retorna el impacto de transfers activos hacia la sucursal del usuario (stock en tránsito).
     */
    @GetMapping("/active-transfers-impact")
    public ResponseEntity<List<TransferImpactDto>> activeTransfersImpact() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID branchId = null;
        if (auth != null && auth.getDetails() != null) {
            try {
                branchId = UUID.fromString(String.valueOf(auth.getDetails()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (branchId == null) return ResponseEntity.badRequest().build();

        List<TransferImpactDto> resp = metricService.getActiveTransfersImpact(branchId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint que devuelve alertas de bajo stock filtrado por la sucursal del usuario.
     * Devuelve una lista de InventoryLowStockDto para alimentar widgets del dashboard.
     */
    @GetMapping("/low-stock-alerts")
    public ResponseEntity<List<InventoryLowStockDto>> lowStockAlerts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID branchId = null;
        if (auth != null && auth.getDetails() != null) {
            try {
                branchId = UUID.fromString(String.valueOf(auth.getDetails()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (branchId == null) return ResponseEntity.badRequest().build();

        List<InventoryLowStockDto> list = inventoryService.getLowStockAlerts(branchId);
        return ResponseEntity.ok(list);
    }

    /**
     * Comparativa de rendimiento entre sucursales. Solo accesible por ADMIN.
     */
    @GetMapping("/branch-performance-comparison")
    public ResponseEntity<Page<BranchPerformanceDto>> branchPerformanceComparison(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<BranchPerformanceDto> page = metricService.getBranchPerformanceComparison(pageable);
        return ResponseEntity.ok(page);
    }

}
