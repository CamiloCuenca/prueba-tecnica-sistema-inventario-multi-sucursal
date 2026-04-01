package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    Page<Sale> findByBranchId(UUID branchId, Pageable pageable);

    @Query("SELECT s FROM Sale s WHERE s.branch.id = :branchId AND s.createdAt >= :from AND s.createdAt <= :to")
    Page<Sale> findByBranchIdAndCreatedAtBetween(@Param("branchId") UUID branchId, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

}

