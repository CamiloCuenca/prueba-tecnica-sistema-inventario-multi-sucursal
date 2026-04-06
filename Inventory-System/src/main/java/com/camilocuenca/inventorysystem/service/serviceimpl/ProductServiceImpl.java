package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.product.ProductDto;
import com.camilocuenca.inventorysystem.model.Product;
import com.camilocuenca.inventorysystem.model.Provider;
import com.camilocuenca.inventorysystem.repository.ProductRepository;
import com.camilocuenca.inventorysystem.repository.ProviderRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProviderRepository providerRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, ProviderRepository providerRepository) {
        this.productRepository = productRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    @Transactional
    public ProductDto createProduct(ProductDto dto) {
        Product p = new Product();
        BeanUtils.copyProperties(dto, p);
        // Asociar providers
        if (dto.getProviderIds() != null && !dto.getProviderIds().isEmpty()) {
            Set<Provider> providers = dto.getProviderIds().stream()
                    .map(id -> providerRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            p.setProviders(providers);
        }
        // Ensure createdAt is set
        if (p.getCreatedAt() == null) p.setCreatedAt(Instant.now());

        Product saved = productRepository.save(p);
        ProductDto out = new ProductDto();
        BeanUtils.copyProperties(saved, out);
        if (saved.getProviders() != null) {
            out.setProviderIds(saved.getProviders().stream().map(Provider::getId).collect(Collectors.toSet()));
        }
        return out;
    }

    @Override
    @Transactional
    public ProductDto updateProduct(UUID id, ProductDto dto) throws Exception {
        Product existing = productRepository.findById(id).orElseThrow(() -> new Exception("Producto no encontrado"));
        existing.setName(dto.getName());
        existing.setSku(dto.getSku());
        existing.setUnit(dto.getUnit());
        if (dto.getProviderIds() != null) {
            Set<Provider> providers = dto.getProviderIds().stream()
                    .map(pid -> providerRepository.findById(pid).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            existing.setProviders(providers);
        }
        Product saved = productRepository.save(existing);
        ProductDto out = new ProductDto();
        BeanUtils.copyProperties(saved, out);
        if (saved.getProviders() != null) {
            out.setProviderIds(saved.getProviders().stream().map(Provider::getId).collect(Collectors.toSet()));
        }
        return out;
    }

    @Override
    @Transactional
    public ProductDto addProviderToProduct(UUID productId, UUID providerId) throws Exception {
        Product product = productRepository.findById(productId).orElseThrow(() -> new Exception("Producto no encontrado"));
        Provider prov = providerRepository.findById(providerId).orElseThrow(() -> new Exception("Proveedor no encontrado"));
        Set<Provider> set = product.getProviders() != null ? product.getProviders() : new java.util.HashSet<>();
        set.add(prov);
        product.setProviders(set);
        Product saved = productRepository.save(product);
        ProductDto out = new ProductDto();
        BeanUtils.copyProperties(saved, out);
        if (saved.getProviders() != null) out.setProviderIds(saved.getProviders().stream().map(Provider::getId).collect(Collectors.toSet()));
        return out;
    }

    @Override
    @Transactional
    public ProductDto removeProviderFromProduct(UUID productId, UUID providerId) throws Exception {
        Product product = productRepository.findById(productId).orElseThrow(() -> new Exception("Producto no encontrado"));
        Provider prov = providerRepository.findById(providerId).orElseThrow(() -> new Exception("Proveedor no encontrado"));
        if (product.getProviders() != null) {
            product.getProviders().removeIf(p -> p.getId().equals(prov.getId()));
        }
        Product saved = productRepository.save(product);
        ProductDto out = new ProductDto();
        BeanUtils.copyProperties(saved, out);
        if (saved.getProviders() != null) out.setProviderIds(saved.getProviders().stream().map(Provider::getId).collect(Collectors.toSet()));
        return out;
    }

    @Override
    public ProductDto getProductById(UUID id) throws Exception {
        Product p = productRepository.findById(id).orElseThrow(() -> new Exception("Producto no encontrado"));
        ProductDto out = new ProductDto();
        BeanUtils.copyProperties(p, out);
        if (p.getProviders() != null) out.setProviderIds(p.getProviders().stream().map(Provider::getId).collect(Collectors.toSet()));
        return out;
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) throws Exception {
        Product p = productRepository.findById(id).orElseThrow(() -> new Exception("Producto no encontrado"));
        productRepository.delete(p);
    }

    @Override
    public Page<ProductDto> listProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<ProductDto> dtos = page.stream().map(p -> {
            ProductDto dto = new ProductDto();
            BeanUtils.copyProperties(p, dto);
            if (p.getProviders() != null) dto.setProviderIds(p.getProviders().stream().map(Provider::getId).collect(Collectors.toSet()));
            return dto;
        }).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    @Transactional
    public void importProductsFromCsv(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) throw new Exception("Archivo CSV vacío");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return;
            String[] headers = headerLine.split(",");
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                idx.put(headers[i].trim().toLowerCase(), i);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] cols = line.split(",");
                String name = getColumn(cols, idx, "name");
                String sku = getColumn(cols, idx, "sku");
                String unit = getColumn(cols, idx, "unit");
                String providerIds = getColumn(cols, idx, "provider_ids");

                Product p = new Product();
                p.setName(name);
                p.setSku(sku);
                p.setUnit(unit);
                // Ensure createdAt is set
                p.setCreatedAt(Instant.now());
                if (providerIds != null && !providerIds.isEmpty()) {
                    String[] parts = providerIds.split(";");
                    Set<Provider> providers = Arrays.stream(parts)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(UUID::fromString)
                            .map(pid -> providerRepository.findById(pid).orElse(null))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());
                    p.setProviders(providers);
                }
                productRepository.save(p);
            }
        }
    }

    private String getColumn(String[] cols, Map<String, Integer> idx, String key) {
        Integer i = idx.get(key);
        if (i == null || i >= cols.length) return null;
        String v = cols[i].trim();
        return v.isEmpty() ? null : v;
    }

    @Override
    public String getCsvTemplate() {
        return "name,sku,unit,provider_ids\nProducto ejemplo,SKU-001,unidad,00000000-0000-0000-0000-000000000000;00000000-0000-0000-0000-000000000001\n";
    }
}
