package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {
    // Buscar alerts por product, branch y urgency que fueron notificadas desde una fecha
    List<StockAlert> findByProductIdAndBranchIdAndUrgencyAndNotifiedAtAfter(UUID productId, UUID branchId, String urgency, Instant after);

    // Buscar alerts recientes para un branch y producto
    List<StockAlert> findByProductIdAndBranchId(UUID productId, UUID branchId);
}

