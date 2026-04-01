package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.TransferAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransferAlertRepository extends JpaRepository<TransferAlert, UUID> {
}

