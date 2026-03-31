package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
    Page<Purchase> findByBranchId(UUID branchId, Pageable pageable);
}
