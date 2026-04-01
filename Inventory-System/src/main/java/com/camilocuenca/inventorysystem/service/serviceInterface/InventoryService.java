package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.inventory.InventoryViewDto;
import com.camilocuenca.inventorysystem.dto.inventory.ProductCatalogItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface InventoryService {
    /**
     * Obtiene el catálogo de productos de la sucursal del usuario solicitante, con opciones de búsqueda y filtrado.
     * @param requesterUserId
     * @param pageable
     * @param q
     * @param showEmpty
     * @return
     */
    Page<ProductCatalogItemDto> getOwnBranchCatalog(UUID requesterUserId, Pageable pageable, String q, boolean showEmpty);

    /**
     *  Obtiene el inventario de una sucursal específica, con opciones de búsqueda y paginación.
     * @param requesterUserId
     * @param branchId
     * @param pageable
     * @param q
     * @return
     */
    Page<InventoryViewDto> getBranchInventory(UUID requesterUserId, UUID branchId, Pageable pageable, String q);

    /**
     *  Obtiene el inventario de un producto específico en una sucursal específica.
     * @param requesterUserId
     * @param branchId
     * @param productId
     * @return
     */
    Optional<InventoryViewDto> getProductInventoryInBranch(UUID requesterUserId, UUID branchId, UUID productId);


    /* Obtener el inventario de todo el sistemas y todas las sucursales para un producto específico.
     * @param requesterUserId
     * @param productId
     * @return
     */
    Page<InventoryViewDto> getProductInventoryInAllBranches(UUID requesterUserId, UUID productId, Pageable pageable);

    /**
     * Recalcula y actualiza el costo promedio ponderado del inventario para un producto en una sucursal
     * usando la fórmula: newAvg = (currentQty * currentAvg + purchaseQty * purchasePrice) / (currentQty + purchaseQty)
     * Si el stock inicial es cero, se establece averageCost = purchasePrice.
     * @param productId id del producto
     * @param branchId id de la sucursal
     * @param purchaseQuantity cantidad comprada (BigDecimal, debe ser > 0)
     * @param purchasePrice precio de compra por unidad (BigDecimal, debe ser >= 0)
     */
    void updateAverageCost(UUID productId, UUID branchId, java.math.BigDecimal purchaseQuantity, java.math.BigDecimal purchasePrice);

    /**
     * Actualiza el precio de venta (salePrice) de un producto en una sucursal específica.
     * @param productId id del producto
     * @param branchId id de la sucursal
     * @param salePrice nuevo precio de venta (BigDecimal, >= 0)
     */
    void updateSalePrice(UUID productId, UUID branchId, java.math.BigDecimal salePrice);
}
