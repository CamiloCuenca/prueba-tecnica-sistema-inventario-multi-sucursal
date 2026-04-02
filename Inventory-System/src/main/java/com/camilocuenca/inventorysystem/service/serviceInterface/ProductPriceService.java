package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.product.ProductPriceDto;

import java.util.List;
import java.util.UUID;

public interface ProductPriceService {
    List<ProductPriceDto> getPricesForProduct(UUID productId);
    ProductPriceDto createPrice(ProductPriceDto dto);
    ProductPriceDto updatePrice(UUID id, ProductPriceDto dto);
    void deletePrice(UUID id);
}

