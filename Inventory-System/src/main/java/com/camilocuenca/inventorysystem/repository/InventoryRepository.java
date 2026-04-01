package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}
