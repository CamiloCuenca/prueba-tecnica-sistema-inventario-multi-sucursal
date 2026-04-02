package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.product.ProductPriceDto;
import com.camilocuenca.inventorysystem.model.ProductPrice;
import com.camilocuenca.inventorysystem.model.Product;
import com.camilocuenca.inventorysystem.repository.ProductPriceRepository;
import com.camilocuenca.inventorysystem.repository.ProductRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProductPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductPriceServiceImpl implements ProductPriceService {

    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ProductPriceServiceImpl(ProductPriceRepository productPriceRepository, ProductRepository productRepository) {
        this.productPriceRepository = productPriceRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductPriceDto> getPricesForProduct(UUID productId) {
        productRepository.findById(productId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        List<ProductPrice> list = productPriceRepository.findByProductIdOrderByPriceAsc(productId);
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ProductPriceDto createPrice(ProductPriceDto dto) {
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        ProductPrice pp = new ProductPrice();
        pp.setProduct(product);
        pp.setLabel(dto.getLabel());
        pp.setPrice(dto.getPrice());
        pp.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "COP");
        pp.setCreatedAt(Instant.now());
        pp.setEffectiveFrom(dto.getEffectiveFrom());
        pp.setEffectiveTo(dto.getEffectiveTo());
        pp = productPriceRepository.save(pp);
        return toDto(pp);
    }

    @Override
    public ProductPriceDto updatePrice(UUID id, ProductPriceDto dto) {
        ProductPrice pp = productPriceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));
        if (dto.getLabel() != null) pp.setLabel(dto.getLabel());
        if (dto.getPrice() != null) pp.setPrice(dto.getPrice());
        if (dto.getCurrency() != null) pp.setCurrency(dto.getCurrency());
        pp.setEffectiveFrom(dto.getEffectiveFrom());
        pp.setEffectiveTo(dto.getEffectiveTo());
        pp = productPriceRepository.save(pp);
        return toDto(pp);
    }

    @Override
    public void deletePrice(UUID id) {
        productPriceRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Price not found"));
        productPriceRepository.deleteById(id);
    }

    private ProductPriceDto toDto(ProductPrice p) {
        ProductPriceDto dto = new ProductPriceDto();
        dto.setId(p.getId());
        dto.setProductId(p.getProduct().getId());
        dto.setLabel(p.getLabel());
        dto.setPrice(p.getPrice());
        dto.setCurrency(p.getCurrency());
        dto.setEffectiveFrom(p.getEffectiveFrom());
        dto.setEffectiveTo(p.getEffectiveTo());
        return dto;
    }
}

