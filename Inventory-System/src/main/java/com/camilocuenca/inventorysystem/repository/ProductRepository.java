package com.camilocuenca.inventorysystem.repository;

import com.camilocuenca.inventorysystem.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("select p from Product p join p.providers pr where pr.id = :providerId")
    Page<Product> findByProviderId(@Param("providerId") UUID providerId, Pageable pageable);

    List<Product> findBySkuIn(List<String> skus);
}
