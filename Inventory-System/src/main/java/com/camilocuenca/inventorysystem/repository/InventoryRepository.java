package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Inventory;
import com.camilocuenca.inventorysystem.dto.metrics.InventoryLowStockDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Page<Inventory> findByBranchId(UUID branchId, Pageable pageable);

    Optional<Inventory> findByBranchIdAndProductId(UUID branchId, UUID productId);

    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE i.branch.id = :branchId AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :q, '%'))) ")
    Page<Inventory> searchByBranchAndProductNameOrSku(@Param("branchId") UUID branchId, @Param("q") String q, Pageable pageable);

    // Nuevo: buscar inventarios por producto en todas las sucursales (paginado)
    Page<Inventory> findByProductId(UUID productId, Pageable pageable);

    // Decremento atómico de stock para evitar sobreventa. Retorna el número de filas afectadas (0 = insuficiente stock)
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity - :qty WHERE i.branch.id = :branchId AND i.product.id = :productId AND i.quantity >= :qty")
    int decrementQuantity(@Param("branchId") UUID branchId, @Param("productId") UUID productId, @Param("qty") java.math.BigDecimal qty);

    // Incremento atómico de stock (para recepción)
    @Modifying
    @Query("UPDATE Inventory i SET i.quantity = i.quantity + :qty WHERE i.branch.id = :branchId AND i.product.id = :productId")
    int incrementQuantity(@Param("branchId") UUID branchId, @Param("productId") UUID productId, @Param("qty") java.math.BigDecimal qty);

    // Buscar registros cuyo quantity <= minStock para la sucursal (eficiente en BD)
    @Query("SELECT i FROM Inventory i WHERE i.branch.id = :branchId AND COALESCE(i.minStock, 0) >= i.quantity")
    Page<Inventory> findLowStockByBranch(@Param("branchId") UUID branchId, Pageable pageable);

    // Nueva consulta que devuelve directamente el DTO con prioridad (urgency) calculada y datos del proveedor.
    @Query("SELECT new com.camilocuenca.inventorysystem.dto.metrics.InventoryLowStockDto(" +
            "p.id, p.name, p.sku, " +
            "CAST(i.quantity AS integer), CAST(COALESCE(i.minStock, 0) AS integer), " +
            "(CAST(COALESCE(i.minStock, 0) AS integer) - CAST(i.quantity AS integer)), " +
            "NULL, p.provider.name, " +
            "CASE WHEN i.quantity = 0 THEN 'CRÍTICO' WHEN (i.quantity * 2) <= COALESCE(i.minStock, 0) THEN 'ALTO' ELSE 'MEDIO' END) " +
            "FROM Inventory i JOIN i.product p LEFT JOIN p.provider pr WHERE i.branch.id = :branchId AND COALESCE(i.minStock,0) >= i.quantity AND p IS NOT NULL ORDER BY " +
            "CASE WHEN i.quantity = 0 THEN 0 WHEN (i.quantity * 2) <= COALESCE(i.minStock,0) THEN 1 ELSE 2 END, i.quantity ASC")
    List<InventoryLowStockDto> findLowStockAlertsByBranch(@Param("branchId") UUID branchId);

}
