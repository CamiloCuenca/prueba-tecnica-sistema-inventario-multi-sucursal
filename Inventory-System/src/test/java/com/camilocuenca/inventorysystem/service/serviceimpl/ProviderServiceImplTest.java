package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto;
import com.camilocuenca.inventorysystem.dto.provider.ProviderDto;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProviderServiceImplTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProviderServiceImpl providerService;

    @Captor
    private ArgumentCaptor<Provider> providerCaptor;

    private UUID providerId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        providerId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    @Test
    void createProvider_success() throws Exception {
        ProviderDto dto = new ProviderDto();
        dto.setName("Prov A");
        dto.setContactInfo("contacto@prov.com");

        Provider saved = new Provider();
        saved.setId(providerId);
        saved.setName(dto.getName());
        saved.setContactInfo(dto.getContactInfo());

        when(providerRepository.save(any(Provider.class))).thenReturn(saved);

        ProviderDto out = providerService.createProvider(dto);

        assertNotNull(out);
        assertEquals(providerId, out.getId());
        assertEquals("Prov A", out.getName());

        verify(providerRepository, times(1)).save(providerCaptor.capture());
        Provider captured = providerCaptor.getValue();
        assertEquals("Prov A", captured.getName());
    }

    @Test
    void updateProvider_success() throws Exception {
        ProviderDto dto = new ProviderDto(); dto.setName("New Name"); dto.setContactInfo("x@a.com");
        Provider existing = new Provider(); existing.setId(providerId); existing.setName("Old");
        Provider saved = new Provider(); saved.setId(providerId); saved.setName(dto.getName()); saved.setContactInfo(dto.getContactInfo());

        when(providerRepository.findById(providerId)).thenReturn(Optional.of(existing));
        when(providerRepository.save(existing)).thenReturn(saved);

        ProviderDto out = providerService.updateProvider(providerId, dto);

        assertNotNull(out);
        assertEquals(dto.getName(), out.getName());
        verify(providerRepository, times(1)).findById(providerId);
        verify(providerRepository, times(1)).save(existing);
    }

    @Test
    void updateProvider_notFound_throws() {
        ProviderDto dto = new ProviderDto();
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> providerService.updateProvider(providerId, dto));
    }

    @Test
    void getProviderById_found() throws Exception {
        Provider p = new Provider(); p.setId(providerId); p.setName("P"); p.setContactInfo("c");
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(p));

        ProviderDto dto = providerService.getProviderById(providerId);
        assertNotNull(dto);
        assertEquals(providerId, dto.getId());
        assertEquals("P", dto.getName());
    }

    @Test
    void getProviderById_notFound_throws() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> providerService.getProviderById(providerId));
    }

    @Test
    void deleteProvider_success() throws Exception {
        Provider p = new Provider(); p.setId(providerId);
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(p));

        assertDoesNotThrow(() -> providerService.deleteProvider(providerId));
        verify(providerRepository, times(1)).delete(p);
    }

    @Test
    void deleteProvider_notFound_throws() {
        when(providerRepository.findById(providerId)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> providerService.deleteProvider(providerId));
    }

    @Test
    void listProviders_returnsPage() {
        Provider p1 = new Provider(); p1.setId(UUID.randomUUID()); p1.setName("A");
        Provider p2 = new Provider(); p2.setId(UUID.randomUUID()); p2.setName("B");
        List<Provider> list = List.of(p1, p2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Provider> page = new PageImpl<>(list, pageable, list.size());
        when(providerRepository.findAll(pageable)).thenReturn(page);

        var res = providerService.listProviders(pageable);
        assertNotNull(res);
        assertEquals(2, res.getContent().size());
        verify(providerRepository, times(1)).findAll(pageable);
    }

    @Test
    void listProductsByProvider_returnsPage() {
        Product p = new Product(); p.setId(productId); p.setName("Prod"); p.setSku("S"); p.setUnit("u");
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByProviderId(providerId, pageable)).thenReturn(new PageImpl<>(List.of(p), pageable, 1));

        Page<ProductByProviderDto> res = providerService.listProductsByProvider(providerId, pageable);
        assertEquals(1, res.getContent().size());
        ProductByProviderDto dto = res.getContent().get(0);
        assertEquals(productId, dto.getProductId());
    }

}

