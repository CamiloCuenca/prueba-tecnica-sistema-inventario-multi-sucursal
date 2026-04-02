package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findByProviderId(UUID providerId, Pageable pageable);
}
