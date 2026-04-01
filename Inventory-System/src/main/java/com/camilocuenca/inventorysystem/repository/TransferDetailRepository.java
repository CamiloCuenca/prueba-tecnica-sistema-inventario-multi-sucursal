package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.TransferDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferDetailRepository extends JpaRepository<TransferDetail, UUID> {

    List<TransferDetail> findByTransferId(UUID transferId);

    Page<TransferDetail> findByTransferId(UUID transferId, Pageable pageable);

}

