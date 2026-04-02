package com.camilocuenca.inventorysystem.service.serviceInterface

import com.camilocuenca.inventorysystem.dto.metrics.BranchPerformanceDto
import com.camilocuenca.inventorysystem.dto.metrics.InventoryBehaviorDto
import com.camilocuenca.inventorysystem.dto.metrics.SalesVolumeDto
import com.camilocuenca.inventorysystem.dto.metrics.TransferImpactDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

/**
 * Interfaz de servicios de métricas del sistema.
 * Cada método que devuelve una colección está paginado según las reglas del proyecto.
 */
interface metricService {


    /**
     * Volumen de ventas del mes en curso vs meses anteriores.
     *
     * Parámetros:
     *  - branchId: UUID opcional para filtrar por sucursal
     *  - productId: UUID opcional para filtrar por producto
     *  - from/to: rango de fechas (opcional). Si ambos son nulos se asume último mes por defecto en la implementación.
     *  - pageable: paginación y ordenamiento solicitados por el cliente
     *
     * Retorna: página de objetos SalesVolumeDto que contienen el total de unidades y monto por agrupación (sucursal/producto)
     */
    fun salesVolume(branchId: UUID?, productId: UUID?, from: LocalDate?, to: LocalDate?, pageable: Pageable): Page<SalesVolumeDto>


    /**  Comportamiento del inventario: rotación, productos de alta y baja demanda.
     *   Retorna un objeto InventoryBehaviorDto que contiene:
     *    - índice de rotación por producto (total vendido / stock actual)
     *    - top 5 productos de alta demanda y top 5 de baja demanda (filtrado por sucursal)
     */
    fun getInventoryBehavior(branchId: UUID?): InventoryBehaviorDto

    /**
     * Impacto de transfers activos hacia la sucursal: lista de transfers y las cantidades por producto en tránsito.
     * Retorna lista de TransferImpactDto (por transfer) con items agregados.
     */
    fun getActiveTransfersImpact(destinationBranchId: UUID?): List<TransferImpactDto>

    /**
     * Comparativa de rendimiento entre sucursales (visible solo para ADMIN).
     * Retorna Page<BranchPerformanceDto> con totalRevenue por sucursal para el mes actual.
     */
    fun getBranchPerformanceComparison(pageable: Pageable): Page<BranchPerformanceDto>

}