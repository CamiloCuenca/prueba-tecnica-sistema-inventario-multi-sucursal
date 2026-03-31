package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto;
import com.camilocuenca.inventorysystem.dto.inventory.ProductCatalogItemDto;
import com.camilocuenca.inventorysystem.model.Branch;
import com.camilocuenca.inventorysystem.model.Inventory;
import com.camilocuenca.inventorysystem.model.Product;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.repository.BranchRepository;
import com.camilocuenca.inventorysystem.repository.InventoryRepository;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository, UserRepository userRepository, BranchRepository branchRepository) {
        this.inventoryRepository = inventoryRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }

    @Override
    public Page<ProductCatalogItemDto> getOwnBranchCatalog(UUID requesterUserId, Pageable pageable, String q, boolean showEmpty) {
        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (user.getBranch() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no tiene sucursal asignada");
        }
        UUID branchId = user.getBranch().getId();
        Page<Inventory> page;
        if (q != null && !q.isEmpty()) {
            page = inventoryRepository.searchByBranchAndProductNameOrSku(branchId, q, pageable);
        } else {
            page = inventoryRepository.findByBranchId(branchId, pageable);
        }

        List<ProductCatalogItemDto> dtoList = page.stream()
                .map(this::toProductCatalogDto)
                .filter(d -> showEmpty || (d.getQuantity() != null && d.getQuantity().compareTo(java.math.BigDecimal.ZERO) > 0))
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    public Page<InventoryViewDto> getBranchInventory(UUID requesterUserId, UUID branchId, Pageable pageable, String q) {
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));

        // Permission check: operators only their own branch
        if (requester.getRole() == Role.OPERATOR && (requester.getBranch() == null || !requester.getBranch().getId().equals(branchId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado a la sucursal solicitada");
        }

        Page<Inventory> page;
        if (q != null && !q.isEmpty()) {
            page = inventoryRepository.searchByBranchAndProductNameOrSku(branchId, q, pageable);
        } else {
            page = inventoryRepository.findByBranchId(branchId, pageable);
        }

        List<InventoryViewDto> dtos = page.stream()
                .map(i -> toInventoryViewDto(i, branch))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public Optional<InventoryViewDto> getProductInventoryInBranch(UUID requesterUserId, UUID branchId, UUID productId) {
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));

        if (requester.getRole() == Role.OPERATOR && (requester.getBranch() == null || !requester.getBranch().getId().equals(branchId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado a la sucursal solicitada");
        }

        Optional<Inventory> inv = inventoryRepository.findByBranchIdAndProductId(branchId, productId);
        return inv.map(i -> toInventoryViewDto(i, branch));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateAverageCost(UUID productId, UUID branchId, java.math.BigDecimal purchaseQuantity, java.math.BigDecimal purchasePrice) {
        if (purchaseQuantity == null || purchasePrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purchaseQuantity y purchasePrice son requeridos");
        }
        if (purchaseQuantity.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purchaseQuantity debe ser mayor que cero");
        }
        if (purchasePrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purchasePrice no puede ser negativo");
        }

        Inventory inventory = inventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de inventario no encontrado para productId/branchId"));

        java.math.BigDecimal currentQty = inventory.getQuantity() != null ? inventory.getQuantity() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal currentAvg = inventory.getAverageCost() != null ? inventory.getAverageCost() : java.math.BigDecimal.ZERO;

        // Si el stock inicial es cero, el nuevo promedio es el precio de compra
        java.math.BigDecimal newAvg;
        java.math.BigDecimal totalQty = currentQty.add(purchaseQuantity);
        if (currentQty.compareTo(java.math.BigDecimal.ZERO) == 0) {
            newAvg = purchasePrice;
        } else {
            // (currentQty * currentAvg + purchaseQty * purchasePrice) / (currentQty + purchaseQty)
            java.math.BigDecimal numerator = currentQty.multiply(currentAvg).add(purchaseQuantity.multiply(purchasePrice));
            newAvg = numerator.divide(totalQty, 6, java.math.RoundingMode.HALF_UP); // mantener escala razonable internamente
        }

        // Redondear a 2 decimales para almacenar (moneda)
        newAvg = newAvg.setScale(2, java.math.RoundingMode.HALF_UP);

        inventory.setAverageCost(newAvg);
        inventoryRepository.save(inventory);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void updateSalePrice(UUID productId, UUID branchId, java.math.BigDecimal salePrice) {
        if (salePrice == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salePrice es requerido");
        }
        if (salePrice.compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salePrice no puede ser negativo");
        }

        Inventory inventory = inventoryRepository.findByBranchIdAndProductId(branchId, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de inventario no encontrado para productId/branchId"));

        inventory.setSalePrice(salePrice.setScale(2, java.math.RoundingMode.HALF_UP));
        inventoryRepository.save(inventory);
    }

    private ProductCatalogItemDto toProductCatalogDto(Inventory inv) {
        Product p = inv.getProduct();
        ProductCatalogItemDto dto = new ProductCatalogItemDto();
        dto.setProductId(p.getId());
        dto.setSku(p.getSku());
        dto.setName(p.getName());
        dto.setUnit(p.getUnit());
        dto.setQuantity(inv.getQuantity());
        // price left null if not modeled
        return dto;
    }

    private InventoryViewDto toInventoryViewDto(Inventory i, Branch branch) {
        Product p = i.getProduct();
        InventoryViewDto dto = new InventoryViewDto();
        dto.setBranchId(branch.getId());
        dto.setBranchName(branch.getName());
        dto.setProductId(p.getId());
        dto.setSku(p.getSku());
        dto.setProductName(p.getName());
        dto.setQuantity(i.getQuantity());
        dto.setUpdatedAt(i.getUpdatedAt());
        return dto;
    }
}

