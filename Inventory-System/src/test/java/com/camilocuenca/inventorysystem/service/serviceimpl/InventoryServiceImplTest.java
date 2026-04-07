package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.dto.branch.BranchDto;
import com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto;
import com.camilocuenca.inventorysystem.dto.inventory.ProductCatalogItemDto;
import com.camilocuenca.inventorysystem.dto.metrics.InventoryLowStockDto;
import com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Captor
    private ArgumentCaptor<Inventory> inventoryCaptor;

    private UUID userId;
    private UUID branchId;
    private UUID productId;
    private UUID providerId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        branchId = UUID.randomUUID();
        productId = UUID.randomUUID();
        providerId = UUID.randomUUID();
    }

    @Test
    void getOwnBranchCatalog_success() {
        // user with branch
        User user = new User();
        Branch b = new Branch(); b.setId(branchId); b.setName("B");
        user.setId(userId); user.setBranch(b);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Product p = new Product(); p.setId(productId); p.setName("P1"); p.setSku("SKU1"); p.setUnit("u");
        Inventory inv = new Inventory(); inv.setProduct(p); inv.setQuantity(new BigDecimal("10")); inv.setBranch(b);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> page = new PageImpl<>(List.of(inv), pageable, 1);
        when(inventoryRepository.findByBranchId(branchId, pageable)).thenReturn(page);

        Page<ProductCatalogItemDto> res = inventoryService.getOwnBranchCatalog(userId, pageable, null, false);
        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        ProductCatalogItemDto dto = res.getContent().get(0);
        assertEquals(productId, dto.getProductId());
        assertEquals("SKU1", dto.getSku());
    }

    @Test
    void getBranchInventory_forbiddenForOperator() {
        User user = new User(); user.setId(userId); user.setRole(Role.OPERATOR);
        Branch other = new Branch(); other.setId(UUID.randomUUID()); other.setName("X");
        user.setBranch(other);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID targetBranch = branchId;
        when(branchRepository.findById(targetBranch)).thenReturn(Optional.of(new Branch()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> inventoryService.getBranchInventory(userId, targetBranch, PageRequest.of(0,10), null));
        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void getProductInventoryInBranch_found() {
        User user = new User(); user.setId(userId); user.setRole(Role.MANAGER);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Branch b = new Branch(); b.setId(branchId); b.setName("Main");
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(b));

        Product p = new Product(); p.setId(productId); p.setName("Prod");
        Inventory inv = new Inventory(); inv.setProduct(p); inv.setBranch(b); inv.setQuantity(new BigDecimal("5"));
        when(inventoryRepository.findByBranchIdAndProductId(branchId, productId)).thenReturn(Optional.of(inv));

        Optional<InventoryViewDto> opt = inventoryService.getProductInventoryInBranch(userId, branchId, productId);
        assertTrue(opt.isPresent());
        InventoryViewDto dto = opt.get();
        assertEquals(productId, dto.getProductId());
        assertEquals("Prod", dto.getProductName());
        assertEquals(branchId, dto.getBranchId());
    }

    @Test
    void updateAverageCost_currentZero_setsToPurchasePrice() {
        Inventory inv = new Inventory();
        inv.setProduct(new Product()); inv.getProduct().setId(productId);
        inv.setBranch(new Branch()); inv.getBranch().setId(branchId);
        inv.setQuantity(BigDecimal.ZERO);
        inv.setAverageCost(null);

        when(inventoryRepository.findByBranchIdAndProductId(branchId, productId)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.updateAverageCost(productId, branchId, new BigDecimal("5"), new BigDecimal("10.00"));

        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory saved = inventoryCaptor.getValue();
        assertEquals(new BigDecimal("10.00"), saved.getAverageCost());
    }

    @Test
    void updateAverageCost_nonZero_calculatesWeightedAvg() {
        Inventory inv = new Inventory();
        inv.setProduct(new Product()); inv.getProduct().setId(productId);
        inv.setBranch(new Branch()); inv.getBranch().setId(branchId);
        inv.setQuantity(new BigDecimal("10"));
        inv.setAverageCost(new BigDecimal("2.00"));

        when(inventoryRepository.findByBranchIdAndProductId(branchId, productId)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.updateAverageCost(productId, branchId, new BigDecimal("5"), new BigDecimal("3.00"));

        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory saved = inventoryCaptor.getValue();
        assertEquals(new BigDecimal("2.33"), saved.getAverageCost());
    }

    @Test
    void updateAverageCost_badArgs_throws() {
        assertThrows(ResponseStatusException.class, () -> inventoryService.updateAverageCost(productId, branchId, null, new BigDecimal("1")));
        assertThrows(ResponseStatusException.class, () -> inventoryService.updateAverageCost(productId, branchId, new BigDecimal("0"), new BigDecimal("1")));
        assertThrows(ResponseStatusException.class, () -> inventoryService.updateAverageCost(productId, branchId, new BigDecimal("1"), new BigDecimal("-1")));
    }

    @Test
    void updateSalePrice_setsAndSaves() {
        Inventory inv = new Inventory();
        inv.setProduct(new Product()); inv.getProduct().setId(productId);
        inv.setBranch(new Branch()); inv.getBranch().setId(branchId);
        when(inventoryRepository.findByBranchIdAndProductId(branchId, productId)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        inventoryService.updateSalePrice(productId, branchId, new BigDecimal("12.345"));

        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory saved = inventoryCaptor.getValue();
        assertEquals(new BigDecimal("12.35"), saved.getSalePrice());
    }

    @Test
    void updateSalePrice_badArgs_throws() {
        assertThrows(ResponseStatusException.class, () -> inventoryService.updateSalePrice(productId, branchId, null));
        assertThrows(ResponseStatusException.class, () -> inventoryService.updateSalePrice(productId, branchId, new BigDecimal("-0.01")));
    }

    @Test
    void getLowStockAlerts_page_mapsDtos() {
        Branch b = new Branch(); b.setId(branchId); b.setName("X");
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(b));

        Product p = new Product(); p.setId(productId); p.setName("P"); p.setSku("S");
        Provider prov = new Provider(); prov.setId(providerId); prov.setName("ProvX");
        p.setProviders(Set.of(prov));

        Inventory inv = new Inventory(); inv.setProduct(p); inv.setQuantity(BigDecimal.ZERO); inv.setMinStock(new BigDecimal("5"));
        Pageable pageable = PageRequest.of(0,10);
        when(inventoryRepository.findLowStockByBranch(branchId, pageable)).thenReturn(new PageImpl<>(List.of(inv), pageable, 1));

        Page<InventoryLowStockDto> res = inventoryService.getLowStockAlerts(branchId, pageable);
        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        InventoryLowStockDto d = res.getContent().get(0);
        assertEquals("CRÍTICO", d.getUrgencyLevel());
        assertEquals("ProvX", d.getSupplier());
    }

    @Test
    void getLowStockAlerts_list_callsRepo() {
        Branch b = new Branch(); b.setId(branchId);
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(b));
        InventoryLowStockDto dto = new InventoryLowStockDto(); dto.setProductId(productId); dto.setProductName("P");
        when(inventoryRepository.findLowStockAlertsByBranch(branchId)).thenReturn(List.of(dto));

        List<InventoryLowStockDto> res = inventoryService.getLowStockAlerts(branchId);
        assertEquals(1, res.size());
    }

    @Test
    void getAllBranches_returnsDtos() {
        Branch b1 = new Branch(); b1.setId(UUID.randomUUID()); b1.setName("A");
        Branch b2 = new Branch(); b2.setId(UUID.randomUUID()); b2.setName("B");
        when(branchRepository.findAll()).thenReturn(List.of(b1,b2));

        List<BranchDto> res = inventoryService.getAllBranches();
        assertEquals(2, res.size());
        assertEquals("A", res.get(0).getName());
    }

    @Test
    void getProductsByProvider_success() {
        User requester = new User(); requester.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        Provider prov = new Provider(); prov.setId(providerId);
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(prov));

        Product p = new Product(); p.setId(productId); p.setName("Prod"); p.setSku("SKU");
        Pageable pageable = PageRequest.of(0,10);
        when(productRepository.findByProviderId(providerId, pageable)).thenReturn(new PageImpl<>(List.of(p), pageable,1));

        Page<ProductByProviderDto> res = inventoryService.getProductsByProvider(userId, providerId, pageable);
        assertEquals(1, res.getContent().size());
        ProductByProviderDto dto = res.getContent().get(0);
        assertEquals(productId, dto.getProductId());
    }

}

