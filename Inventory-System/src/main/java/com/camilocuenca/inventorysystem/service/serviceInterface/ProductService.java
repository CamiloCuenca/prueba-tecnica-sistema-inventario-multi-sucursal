package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.product.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(ProductDto dto);
    ProductDto updateProduct(UUID id, ProductDto dto) throws Exception;
    ProductDto getProductById(UUID id) throws Exception;
    void deleteProduct(UUID id) throws Exception;
    Page<ProductDto> listProducts(Pageable pageable);
    /**
     * Crea/actualiza productos desde un CSV. CSV columns: name,sku,unit,provider_ids (semicolon separated UUIDs)
     */
    void importProductsFromCsv(MultipartFile file) throws Exception;

    /**
     * Devuelve una plantilla CSV para carga masiva
     */
    String getCsvTemplate();

    // Asociar un proveedor a un producto (ADMIN)
    ProductDto addProviderToProduct(UUID productId, UUID providerId) throws Exception;

    // Remover la asociación de un proveedor a un producto sin borrar el producto globalmente (ADMIN)
    ProductDto removeProviderFromProduct(UUID productId, UUID providerId) throws Exception;
}
