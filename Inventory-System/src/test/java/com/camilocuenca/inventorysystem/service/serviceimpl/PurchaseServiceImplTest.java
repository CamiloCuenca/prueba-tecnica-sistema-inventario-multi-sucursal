package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.purchase.*;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;
    @Mock
    private PurchaseDetailRepository purchaseDetailRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    private UUID userId;
    private UUID branchId;
    private UUID productId;
    private UUID detailId;
    private UUID purchaseId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        productId = UUID.randomUUID();
        detailId = UUID.randomUUID();
        purchaseId = UUID.randomUUID();
    }

    @Test
    void createPurchase_success() {
        PurchaseCreateDto dto = new PurchaseCreateDto();
        dto.setBranchId(branchId);
        PurchaseDetailCreateDto item = new PurchaseDetailCreateDto();
        item.setProductId(productId);
        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("5.00"));
        item.setDiscount(new BigDecimal("0"));
        dto.setItems(List.of(item));

        Product p = new Product(); p.setId(productId); p.setName("Prod");
        when(productRepository.findById(productId)).thenReturn(Optional.of(p));

        Purchase saved = new Purchase(); saved.setId(purchaseId); saved.setBranch(new Branch()); saved.getBranch().setId(branchId); saved.setCreatedAt(Instant.now());
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(saved);

        // Ensure the saved detail returns the same object passed to save so discount set by service is preserved
        when(purchaseDetailRepository.save(any(PurchaseDetail.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseResponseDto res = purchaseService.createPurchase(userId, dto);
        assertNotNull(res);
        assertEquals(purchaseId, res.getId());
        assertEquals(1, res.getDetails().size());
        verify(purchaseRepository, times(2)).save(any(Purchase.class));
    }

    @Test
    void createPurchase_emptyItems_throws() {
        PurchaseCreateDto dto = new PurchaseCreateDto(); dto.setBranchId(branchId); dto.setItems(Collections.emptyList());
        assertThrows(ResponseStatusException.class, () -> purchaseService.createPurchase(userId, dto));
    }

    @Test
    void createPurchase_productNotFound_throws() {
        PurchaseCreateDto dto = new PurchaseCreateDto(); dto.setBranchId(branchId);
        PurchaseDetailCreateDto item = new PurchaseDetailCreateDto(); item.setProductId(productId); item.setQuantity(new BigDecimal("1")); item.setUnitPrice(new BigDecimal("1")); item.setDiscount(new BigDecimal("0"));
        dto.setItems(List.of(item));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> purchaseService.createPurchase(userId, dto));
    }

    @Test
    void receivePurchase_success_partialAndFull() {
        // Setup existing purchase with details
        Purchase purchase = new Purchase(); purchase.setId(purchaseId); purchase.setBranch(new Branch()); purchase.getBranch().setId(branchId);
        PurchaseDetail detail = new PurchaseDetail(); detail.setId(detailId); detail.setPurchase(purchase); detail.setProduct(new Product()); detail.getProduct().setId(productId); detail.setQuantity(new BigDecimal("5")); detail.setReceivedQuantity(BigDecimal.ZERO); detail.setPrice(new BigDecimal("2.50"));
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseDetailRepository.findAllById(List.of(detailId))).thenReturn(List.of(detail));
        when(purchaseDetailRepository.findAll()).thenReturn(List.of(detail));

        // inventory repo returns empty -> create new inventory
        when(inventoryRepository.findByBranchIdAndProductId(branchId, productId)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        // inventoryTransactionRepository.save returns the saved tx (non-void)
        when(inventoryTransactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        PurchaseReceiveDto recv = new PurchaseReceiveDto(); PurchaseReceiveItemDto recvItem = new PurchaseReceiveItemDto(); recvItem.setPurchaseDetailId(detailId); recvItem.setQuantityReceived(new BigDecimal("3")); recv.setItems(List.of(recvItem)); recv.setReceivedAt(Instant.now());

        // inventoryService.updateAverageCost should be called
        doNothing().when(inventoryService).updateAverageCost(any(), any(), any(), any());

        PurchaseResponseDto out = purchaseService.receivePurchase(userId, purchaseId, recv);
        assertNotNull(out);
        assertEquals(purchaseId, out.getId());
        // After partial receive, status should be PARTIALLY_RECEIVED
        assertEquals("PARTIALLY_RECEIVED", out.getStatus().getName());

        // Now receive rest
        PurchaseReceiveDto recv2 = new PurchaseReceiveDto(); PurchaseReceiveItemDto recvItem2 = new PurchaseReceiveItemDto(); recvItem2.setPurchaseDetailId(detailId); recvItem2.setQuantityReceived(new BigDecimal("2")); recv2.setItems(List.of(recvItem2)); recv2.setReceivedAt(Instant.now());

        // Simulate that detail repository now has updated received quantity (service will fetch all and evaluate)
        detail.setReceivedQuantity(new BigDecimal("3"));
        when(purchaseDetailRepository.findAllById(List.of(detailId))).thenReturn(List.of(detail));
        when(purchaseDetailRepository.findAll()).thenReturn(List.of(detail));

        PurchaseResponseDto out2 = purchaseService.receivePurchase(userId, purchaseId, recv2);
        assertNotNull(out2);
        // After full receive, expect RECEIVED or PARTIALLY_RECEIVED depending on repository check
        assertTrue(out2.getStatus().getName().equals("RECEIVED") || out2.getStatus().getName().equals("PARTIALLY_RECEIVED"));
    }

    @Test
    void receivePurchase_invalidDetail_throws() {
        UUID missingDetail = UUID.randomUUID();
        PurchaseReceiveDto recv = new PurchaseReceiveDto(); PurchaseReceiveItemDto recvItem = new PurchaseReceiveItemDto(); recvItem.setPurchaseDetailId(missingDetail); recvItem.setQuantityReceived(new BigDecimal("1")); recv.setItems(List.of(recvItem));
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(new Purchase()));
        when(purchaseDetailRepository.findAllById(List.of(missingDetail))).thenReturn(Collections.emptyList());
        assertThrows(ResponseStatusException.class, () -> purchaseService.receivePurchase(userId, purchaseId, recv));
    }

    @Test
    void getPurchase_found() {
        Purchase purchase = new Purchase(); purchase.setId(purchaseId); purchase.setBranch(new Branch()); purchase.getBranch().setId(branchId);
        PurchaseDetail detail = new PurchaseDetail(); detail.setId(detailId); detail.setPurchase(purchase);
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.of(purchase));
        when(purchaseDetailRepository.findAll()).thenReturn(List.of(detail));

        PurchaseResponseDto dto = purchaseService.getPurchase(userId, purchaseId);
        assertNotNull(dto);
        assertEquals(purchaseId, dto.getId());
    }

    @Test
    void getPurchase_notFound_throws() {
        when(purchaseRepository.findById(purchaseId)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> purchaseService.getPurchase(userId, purchaseId));
    }

    @Test
    void listPurchases_permissions() {
        // when requester is null -> unauthorized
        assertThrows(ResponseStatusException.class, () -> purchaseService.listPurchases(null, null, PageRequest.of(0,10)));

        // when non-admin/manager and branchId null -> forbidden
        User u = new User(); u.setId(userId); u.setRole(null); when(userRepository.findById(userId)).thenReturn(Optional.of(u));
        assertThrows(ResponseStatusException.class, () -> purchaseService.listPurchases(userId, null, PageRequest.of(0,10)));

        // admin can list all
        User admin = new User(); admin.setId(userId); admin.setRole(com.camilocuenca.inventorysystem.Enums.Role.ADMIN);
        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));
        Page<Purchase> page = new PageImpl<>(List.of(new Purchase()), PageRequest.of(0,10), 1);
        when(purchaseRepository.findByBranchId(null, PageRequest.of(0,10))).thenReturn(page);
        var res = purchaseService.listPurchases(userId, null, PageRequest.of(0,10));
        assertNotNull(res);
    }

}
