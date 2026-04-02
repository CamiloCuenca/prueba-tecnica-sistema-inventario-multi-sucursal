package com.camilocuenca.inventorysystem.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio custom para consultas de métricas (queries complejas, agregaciones y optimizaciones).
 */
public interface MetricRepositoryCustom {

    /**
     * Ejecuta la consulta agregada de volumen de ventas devolviendo filas crudas.
     * Cada row corresponde a: branch_id, branch_name, product_id, product_name, year, month, total_units, total_revenue
     */
    List<Object[]> findSalesVolume(UUID branchId, UUID productId, Instant fromDate, Instant toDate, int offset, int limit);

    /**
     * Retorna el total de filas (agrupaciones) para la misma consulta, utilizado para paginación.
     */
    long countSalesVolume(UUID branchId, UUID productId, Instant fromDate, Instant toDate);

}

