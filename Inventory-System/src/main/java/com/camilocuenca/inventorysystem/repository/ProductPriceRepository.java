package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {
    List<ProductPrice> findByProductIdOrderByPriceAsc(UUID productId);
}

