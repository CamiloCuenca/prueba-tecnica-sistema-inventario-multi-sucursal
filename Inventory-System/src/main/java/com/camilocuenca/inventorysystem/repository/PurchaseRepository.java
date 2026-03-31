package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {
}
