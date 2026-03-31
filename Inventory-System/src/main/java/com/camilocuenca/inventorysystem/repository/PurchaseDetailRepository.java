package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.PurchaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseDetailRepository extends JpaRepository<PurchaseDetail, UUID> {
}

