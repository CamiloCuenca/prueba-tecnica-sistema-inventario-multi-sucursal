package com.camilocuenca.inventorysystem.service;

import com.camilocuenca.inventorysystem.dto.sale.SaleItemRequestDto;
import com.camilocuenca.inventorysystem.dto.sale.SaleRequestDto;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
import com.camilocuenca.inventorysystem.service.serviceimpl.SaleServiceImpl;
import com.camilocuenca.inventorysystem.exceptions.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SaleServiceImplTest {

    @Mock
    SaleRepository saleRepository;

    @Mock
    SaleDetailRepository saleDetailRepository;

    @Mock
    InventoryRepository inventoryRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    BranchRepository branchRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    SaleServiceImpl saleService;

    UUID branchId;
    UUID productId;
    UUID userId;

    @BeforeEach
    void setup() {
        // reset mocks between tests to avoid cross-test stubbing issues
        reset(saleRepository, saleDetailRepository, inventoryRepository, productRepository, branchRepository, userRepository, inventoryTransactionRepository, eventPublisher);

        branchId = UUID.randomUUID();
        productId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void createSale_success_decrementsInventoryAndReturnsResponse() {
        // preparar DTO
        SaleItemRequestDto item = new SaleItemRequestDto();
        item.setProductId(productId);
        item.setQuantity(new BigDecimal("2"));
        item.setPrice(new BigDecimal("10.00"));

        SaleRequestDto req = new SaleRequestDto();
        req.setBranchId(branchId);
        req.setItems(List.of(item));

        // mocks
        Branch branch = new Branch(); branch.setId(branchId);
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));

        User user = new User(); user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Product product = new Product(); product.setId(productId); product.setName("Prod A");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // inventory decrement should return 1 (rows updated)
        when(inventoryRepository.decrementQuantity(eq(branchId), eq(productId), any(java.math.BigDecimal.class))).thenReturn(1);

        // saleRepository.save -> simulate assigning id
        when(saleRepository.save(any())).thenAnswer(invocation -> {
            Sale s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            s.setTotal(new BigDecimal("20.00"));
            s.setCreatedAt(Instant.now());
            return s;
        });

        when(saleDetailRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(inventoryTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var resp = saleService.createSale(req, userId);

        assertNotNull(resp);
        assertEquals(branchId, resp.getBranchId());
        assertEquals(userId, resp.getUserId());
        assertEquals(new BigDecimal("20.00"), resp.getTotal());

        verify(inventoryRepository).decrementQuantity(eq(branchId), eq(productId), any(java.math.BigDecimal.class));
        verify(saleRepository).save(any());
        verify(saleDetailRepository).save(any());
        verify(inventoryTransactionRepository).save(any());
    }

    @Test
    void createSale_insufficientStock_throwsInsufficientStockException() {
        SaleItemRequestDto item = new SaleItemRequestDto();
        item.setProductId(productId);
        item.setQuantity(new BigDecimal("5"));
        // fijar price para evitar lookup de inventory y forzar decrement
        item.setPrice(new BigDecimal("1.00"));

        SaleRequestDto req = new SaleRequestDto();
        req.setBranchId(branchId);
        req.setItems(List.of(item));

        Branch branch = new Branch(); branch.setId(branchId);
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));

        when(inventoryRepository.decrementQuantity(eq(branchId), eq(productId), any(java.math.BigDecimal.class))).thenReturn(0);

        assertThrows(InsufficientStockException.class, () -> saleService.createSale(req, userId));

        verify(inventoryRepository).decrementQuantity(eq(branchId), eq(productId), any(java.math.BigDecimal.class));
    }

    @Test
    void createSale_productNotAvailableInBranch_throwsResponseStatusException() {
        SaleItemRequestDto item = new SaleItemRequestDto();
        item.setProductId(productId);
        item.setQuantity(new BigDecimal("1"));

        SaleRequestDto req = new SaleRequestDto();
        req.setBranchId(branchId);
        req.setItems(List.of(item));

        Branch branch = new Branch(); branch.setId(branchId);
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        Product product = new Product(); product.setId(productId);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // inventoryRepository.findByBranchIdAndProductId returns empty -> should throw
        doReturn(Optional.empty()).when(inventoryRepository).findByBranchIdAndProductId(eq(branchId), eq(productId));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> saleService.createSale(req, userId));
        assertTrue(ex.getStatusCode().is4xxClientError());
    }
}
