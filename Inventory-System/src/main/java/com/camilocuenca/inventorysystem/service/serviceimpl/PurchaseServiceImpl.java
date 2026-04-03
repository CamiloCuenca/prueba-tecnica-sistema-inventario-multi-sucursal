package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.Enums.PurchaseStatus;
import com.camilocuenca.inventorysystem.dto.purchase.*;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import com.camilocuenca.inventorysystem.service.serviceInterface.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private PurchaseDetailRepository purchaseDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Override
    @Transactional
    public PurchaseResponseDto createPurchase(UUID requesterUserId, PurchaseCreateDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Purchase must have at least one item");
        }

        User user = userRepository.findById(requesterUserId).orElse(null);

        Purchase purchase = new Purchase();
        purchase.setBranch(new Branch());
        purchase.getBranch().setId(dto.getBranchId());
        purchase.setUser(user);
        // Resolver provider si se pasó supplierId, si no, almacenar notas libres
        if (dto.getProviderId() != null) {
            com.camilocuenca.inventorysystem.model.Provider prov = providerRepository.findById(dto.getProviderId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found: " + dto.getProviderId()));
            purchase.setProvider(prov);
        } else {
            purchase.setSupplierNotes(dto.getNotes());
        }
        purchase.setPaymentTerms(dto.getPaymentTerms());
        purchase.setExpectedDeliveryDate(dto.getExpectedDeliveryDate());
        purchase.setCreatedAt(Instant.now());
        purchase.setStatus(PurchaseStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountTotal = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;

        purchase = purchaseRepository.save(purchase);

        List<PurchaseDetail> details = new ArrayList<>();
        for (PurchaseDetailCreateDto item : dto.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + item.getProductId()));

            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be > 0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit price must be >= 0");
            }

            PurchaseDetail detail = new PurchaseDetail();
            detail.setPurchase(purchase);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getUnitPrice());
            detail.setDiscount(item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
            detail.setReceivedQuantity(BigDecimal.ZERO);
            detail = purchaseDetailRepository.save(detail);

            BigDecimal lineTotal = item.getUnitPrice().subtract(detail.getDiscount())
                    .multiply(item.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);
            discountTotal = discountTotal.add(detail.getDiscount().multiply(item.getQuantity()));

            details.add(detail);
        }

        // Simple tax placeholder (0). Integrate tax rules later.

        BigDecimal total = subtotal.subtract(discountTotal).add(tax).setScale(2, RoundingMode.HALF_UP);
        purchase.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        purchase.setDiscountTotal(discountTotal.setScale(2, RoundingMode.HALF_UP));
        purchase.setTax(tax.setScale(2, RoundingMode.HALF_UP));
        purchase.setTotal(total);

        purchase = purchaseRepository.save(purchase);

        return toResponseDto(purchase, details);
    }

    @Override
    @Transactional
    public PurchaseResponseDto receivePurchase(UUID requesterUserId, UUID purchaseId, PurchaseReceiveDto dto) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        Map<UUID, PurchaseDetail> detailMap = purchaseDetailRepository.findAllById(
                dto.getItems().stream().map(PurchaseReceiveItemDto::getPurchaseDetailId).collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(PurchaseDetail::getId, d -> d));

        User user = userRepository.findById(requesterUserId).orElse(null);

        boolean anyReceived = false;
        for (PurchaseReceiveItemDto item : dto.getItems()) {
            PurchaseDetail detail = detailMap.get(item.getPurchaseDetailId());
            if (detail == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase detail not found: " + item.getPurchaseDetailId());
            }
            if (item.getQuantityReceived() == null || item.getQuantityReceived().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received quantity must be > 0");
            }

            BigDecimal newReceived = (detail.getReceivedQuantity() == null ? BigDecimal.ZERO : detail.getReceivedQuantity()).add(item.getQuantityReceived());
            if (newReceived.compareTo(detail.getQuantity()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Received quantity exceeds ordered quantity for detail " + detail.getId());
            }

            // Update inventory: increase quantity and recalculate average cost
            Inventory inventory = inventoryRepository.findByBranchIdAndProductId(purchase.getBranch().getId(), detail.getProduct().getId())
                    .orElseGet(() -> {
                        Inventory inv = new Inventory();
                        inv.setBranch(purchase.getBranch());
                        inv.setProduct(detail.getProduct());
                        inv.setQuantity(BigDecimal.ZERO);
                        inv.setAverageCost(BigDecimal.ZERO);
                        // asegurar valores no nulos para columnas con constraint NOT NULL
                        inv.setSalePrice(BigDecimal.ZERO);
                        inv.setUpdatedAt(Instant.now());
                        return inv;
                    });

            inventory.setQuantity((inventory.getQuantity() == null ? BigDecimal.ZERO : inventory.getQuantity()).add(item.getQuantityReceived()));
            inventoryRepository.save(inventory);

            // Recalculate average cost via inventoryService
            inventoryService.updateAverageCost(detail.getProduct().getId(), purchase.getBranch().getId(), item.getQuantityReceived(), detail.getPrice());

            // Create inventory transaction for audit
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(detail.getProduct());
            tx.setBranch(purchase.getBranch());
            tx.setUser(user);
            tx.setType("PURCHASE_RECEIPT");
            tx.setQuantity(item.getQuantityReceived());
            tx.setReason("Received from purchase " + purchase.getId());
            tx.setReferenceType("PURCHASE");
            tx.setReferenceId(purchase.getId());
            tx.setCreatedAt(dto.getReceivedAt() != null ? dto.getReceivedAt() : Instant.now());
            inventoryTransactionRepository.save(tx);

            detail.setReceivedQuantity(newReceived);
            purchaseDetailRepository.save(detail);
            anyReceived = true;
        }

        if (anyReceived) {
            // Update purchase status
            boolean allReceived = purchaseDetailRepository.findAll().stream()
                    .filter(d -> d.getPurchase().getId().equals(purchase.getId()))
                    .allMatch(d -> d.getReceivedQuantity() != null && d.getReceivedQuantity().compareTo(d.getQuantity()) >= 0);

            purchase.setStatus(allReceived ? PurchaseStatus.RECEIVED : PurchaseStatus.PARTIALLY_RECEIVED);
            purchaseRepository.save(purchase);
        }

        List<PurchaseDetail> details = purchaseDetailRepository.findAll().stream().filter(d -> d.getPurchase().getId().equals(purchase.getId())).collect(Collectors.toList());
        return toResponseDto(purchase, details);
    }

    @Override
    public PurchaseResponseDto getPurchase(UUID requesterUserId, UUID purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"));

        List<PurchaseDetail> details = purchaseDetailRepository.findAll().stream().filter(d -> d.getPurchase().getId().equals(purchase.getId())).collect(Collectors.toList());
        return toResponseDto(purchase, details);
    }

    @Override
    public Page<PurchaseSummaryDto> listPurchases(UUID requesterUserId, UUID branchId, Pageable pageable) {
        if (requesterUserId == null) throw new ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Authentication required");

        com.camilocuenca.inventorysystem.model.User user = userRepository.findById(requesterUserId).orElse(null);
        boolean isAdmin = user != null && user.getRole() != null && user.getRole().name().equals("ADMIN");
        boolean isManager = user != null && user.getRole() != null && user.getRole().name().equals("MANAGER");
        boolean isOperator = user != null && user.getRole() != null && user.getRole().name().equals("OPERATOR");

        if (branchId == null && !(isAdmin || isManager)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Only admin/manager can list all branches");
        }

        org.springframework.data.domain.Page<com.camilocuenca.inventorysystem.model.Purchase> page = purchaseRepository.findByBranchId(branchId, pageable);
        return page.map(this::toSummary);
    }

    private PurchaseResponseDto toResponseDto(Purchase purchase, List<PurchaseDetail> details) {
        PurchaseResponseDto dto = new PurchaseResponseDto();
        dto.setId(purchase.getId());
        dto.setBranchId(purchase.getBranch() != null ? purchase.getBranch().getId() : null);
        // Mantener compatibilidad: si hay provider mostrar su nombre, si no mostrar notas
        String supplierDisplay = null;
        if (purchase.getProvider() != null) {
            supplierDisplay = purchase.getProvider().getName();
        } else {
            supplierDisplay = purchase.getSupplierNotes();
        }
        dto.setSupplier(supplierDisplay);
        PurchaseResponseDto.PurchaseStatusDto st = new PurchaseResponseDto.PurchaseStatusDto();
        st.setName(purchase.getStatus() != null ? purchase.getStatus().name() : null);
        dto.setStatus(st);
        dto.setSubtotal(purchase.getSubtotal());
        dto.setTax(purchase.getTax());
        dto.setDiscountTotal(purchase.getDiscountTotal());
        dto.setTotal(purchase.getTotal());
        dto.setCreatedAt(purchase.getCreatedAt());
        List<PurchaseDetailResponseDto> detailDtos = new ArrayList<>();
        for (PurchaseDetail d : details) {
            PurchaseDetailResponseDto pd = new PurchaseDetailResponseDto();
            pd.setId(d.getId());
            pd.setProductId(d.getProduct() != null ? d.getProduct().getId() : null);
            pd.setOrderedQuantity(d.getQuantity());
            pd.setReceivedQuantity(d.getReceivedQuantity());
            pd.setUnitPrice(d.getPrice());
            pd.setDiscount(d.getDiscount());
            pd.setLineTotal(d.getPrice() != null && d.getQuantity() != null ? d.getPrice().multiply(d.getQuantity()) : null);
            detailDtos.add(pd);
        }
        dto.setDetails(detailDtos);
        return dto;
    }

    private PurchaseSummaryDto toSummary(Purchase purchase) {
        PurchaseSummaryDto dto = new PurchaseSummaryDto();
        dto.setId(purchase.getId());
        dto.setBranchId(purchase.getBranch() != null ? purchase.getBranch().getId() : null);
        // Mostrar nombre del proveedor si existe, sino mostrar notas de proveedor
        String supplierDisplay2 = null;
        if (purchase.getProvider() != null) {
            supplierDisplay2 = purchase.getProvider().getName();
        } else {
            supplierDisplay2 = purchase.getSupplierNotes();
        }
        dto.setSupplier(supplierDisplay2);
        PurchaseSummaryDto.PurchaseStatusDto st = new PurchaseSummaryDto.PurchaseStatusDto();
        st.setName(purchase.getStatus() != null ? purchase.getStatus().name() : null);
        dto.setStatus(st);
        dto.setSubtotal(purchase.getSubtotal());
        dto.setTax(purchase.getTax());
        dto.setDiscountTotal(purchase.getDiscountTotal());
        dto.setTotal(purchase.getTotal());
        dto.setCreatedAt(purchase.getCreatedAt());
        return dto;
    }
}

