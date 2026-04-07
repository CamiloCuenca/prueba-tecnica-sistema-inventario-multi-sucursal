package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.product.ProductDto;
import com.camilocuenca.inventorysystem.model.Product;
import com.camilocuenca.inventorysystem.model.Provider;
import com.camilocuenca.inventorysystem.repository.ProductRepository;
import com.camilocuenca.inventorysystem.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UUID prodId;
    private UUID provId;

    @BeforeEach
    void setUp() {
        prodId = UUID.randomUUID();
        provId = UUID.randomUUID();
    }

    @Test
    void createProduct_withProviders_success() {
        ProductDto dto = new ProductDto();
        dto.setName("Arroz 1kg");
        dto.setSku("ARZ-001");
        dto.setUnit("unidad");
        dto.setProviderIds(Set.of(provId));

        Provider prov = new Provider();
        prov.setId(provId);
        prov.setName("Proveedor X");

        when(providerRepository.findById(provId)).thenReturn(Optional.of(prov));

        Product saved = new Product();
        saved.setId(prodId);
        saved.setName(dto.getName());
        saved.setSku(dto.getSku());
        saved.setUnit(dto.getUnit());
        saved.setCreatedAt(Instant.now());
        saved.setProviders(Set.of(prov));

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductDto out = productService.createProduct(dto);

        assertNotNull(out);
        assertEquals(prodId, out.getId());
        assertEquals("Arroz 1kg", out.getName());
        assertNotNull(out.getProviderIds());
        assertTrue(out.getProviderIds().contains(provId));

        verify(productRepository, times(1)).save(productCaptor.capture());
        Product captured = productCaptor.getValue();
        assertEquals("ARZ-001", captured.getSku());
        assertNotNull(captured.getCreatedAt());
    }

    @Test
    void updateProduct_success() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setName("Arroz 2kg");
        dto.setSku("ARZ-002");
        dto.setUnit("unidad");

        Product existing = new Product();
        existing.setId(prodId);
        existing.setName("Old");

        Provider prov = new Provider(); prov.setId(provId); prov.setName("P");
        when(productRepository.findById(prodId)).thenReturn(Optional.of(existing));
        when(providerRepository.findById(provId)).thenReturn(Optional.of(prov));

        dto.setProviderIds(Set.of(provId));

        Product saved = new Product();
        saved.setId(prodId);
        saved.setName(dto.getName());
        saved.setSku(dto.getSku());
        saved.setUnit(dto.getUnit());
        saved.setProviders(Set.of(prov));
        saved.setCreatedAt(Instant.now());

        when(productRepository.save(existing)).thenReturn(saved);

        ProductDto out = productService.updateProduct(prodId, dto);

        assertNotNull(out);
        assertEquals(prodId, out.getId());
        assertEquals("ARZ-002", out.getSku());
        assertTrue(out.getProviderIds().contains(provId));

        verify(productRepository, times(1)).findById(prodId);
        verify(productRepository, times(1)).save(existing);
    }

    @Test
    void updateProduct_notFound_throws() {
        ProductDto dto = new ProductDto();
        when(productRepository.findById(prodId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> productService.updateProduct(prodId, dto));
    }

    @Test
    void getProductById_found() throws Exception {
        Product p = new Product();
        p.setId(prodId);
        p.setName("Prod A");
        Provider prov = new Provider(); prov.setId(provId); prov.setName("P");
        p.setProviders(Set.of(prov));

        when(productRepository.findById(prodId)).thenReturn(Optional.of(p));

        ProductDto dto = productService.getProductById(prodId);
        assertNotNull(dto);
        assertEquals(prodId, dto.getId());
        assertTrue(dto.getProviderIds().contains(provId));
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(prodId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> productService.getProductById(prodId));
    }

    @Test
    void deleteProduct_success() throws Exception {
        Product p = new Product(); p.setId(prodId);
        when(productRepository.findById(prodId)).thenReturn(Optional.of(p));

        assertDoesNotThrow(() -> productService.deleteProduct(prodId));
        verify(productRepository, times(1)).delete(p);
    }

    @Test
    void deleteProduct_notFound_throws() {
        when(productRepository.findById(prodId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> productService.deleteProduct(prodId));
    }

    @Test
    void listProducts_returnsPage() {
        Product p1 = new Product(); p1.setId(UUID.randomUUID()); p1.setName("A");
        Product p2 = new Product(); p2.setId(UUID.randomUUID()); p2.setName("B");
        List<Product> list = List.of(p1, p2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(list, pageable, list.size());
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductDto> res = productService.listProducts(pageable);
        assertNotNull(res);
        assertEquals(2, res.getContent().size());
        assertEquals(list.size(), res.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void addAndRemoveProvider_success() throws Exception {
        Product p = new Product(); p.setId(prodId); p.setProviders(new HashSet<>());
        Provider prov = new Provider(); prov.setId(provId); prov.setName("P");

        when(productRepository.findById(prodId)).thenReturn(Optional.of(p));
        when(providerRepository.findById(provId)).thenReturn(Optional.of(prov));

        Product savedWithProv = new Product(); savedWithProv.setId(prodId); savedWithProv.setProviders(Set.of(prov));
        when(productRepository.save(any(Product.class))).thenReturn(savedWithProv);

        ProductDto afterAdd = productService.addProviderToProduct(prodId, provId);
        assertTrue(afterAdd.getProviderIds().contains(provId));

        // now remove
        Product savedWithoutProv = new Product(); savedWithoutProv.setId(prodId); savedWithoutProv.setProviders(new HashSet<>());
        when(productRepository.save(any(Product.class))).thenReturn(savedWithoutProv);

        ProductDto afterRemove = productService.removeProviderFromProduct(prodId, provId);
        assertNotNull(afterRemove);
        assertTrue(afterRemove.getProviderIds() == null || afterRemove.getProviderIds().isEmpty());
    }

}

