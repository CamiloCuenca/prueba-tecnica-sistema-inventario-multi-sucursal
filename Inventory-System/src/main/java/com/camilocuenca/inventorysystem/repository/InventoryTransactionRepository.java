package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
}

