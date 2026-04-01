package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Page<Transfer> findByOriginBranchId(UUID originBranchId, Pageable pageable);

    Page<Transfer> findByDestinationBranchId(UUID destinationBranchId, Pageable pageable);

    Page<Transfer> findByStatus(String status, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.originBranch.id = :branchId AND t.createdAt >= :from AND t.createdAt <= :to")
    Page<Transfer> findByOriginBranchIdAndCreatedAtBetween(@Param("branchId") UUID branchId, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("SELECT t FROM Transfer t WHERE t.destinationBranch.id = :branchId AND t.createdAt >= :from AND t.createdAt <= :to")
    Page<Transfer> findByDestinationBranchIdAndCreatedAtBetween(@Param("branchId") UUID branchId, @Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

}

