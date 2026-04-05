package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.sale.SaleItemRequestDto;
import com.camilocuenca.inventorysystem.dto.sale.SaleItemResponseDto;
import com.camilocuenca.inventorysystem.dto.sale.SaleRequestDto;
import com.camilocuenca.inventorysystem.dto.sale.SaleResponseDto;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.repository.*;
import com.camilocuenca.inventorysystem.service.serviceInterface.SaleService;
import com.camilocuenca.inventorysystem.exceptions.InsufficientStockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public SaleServiceImpl(SaleRepository saleRepository,
                           SaleDetailRepository saleDetailRepository,
                           InventoryRepository inventoryRepository,
                           ProductRepository productRepository,
                           BranchRepository branchRepository,
                           UserRepository userRepository,
                           InventoryTransactionRepository inventoryTransactionRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.saleRepository = saleRepository;
        this.saleDetailRepository = saleDetailRepository;
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Crea una venta (cabecera + detalles) validando stock y registrando transacciones de inventario.
     * - Valida existencia de branch, user y productos.
     * - Para cada ítem, decremeta stock usando una operación atómica en la tabla inventory.
     * - Si falta stock para algún ítem: lanza InsufficientStockException y hace rollback de la transacción.
     * - Genera un receiptNumber simple y guarda Sale + SaleDetail + InventoryTransaction.
     *
     * @param req DTO con la información de la venta
     * @return SaleResponseDto con el detalle de la venta creada
     */
    @Transactional
    public SaleResponseDto createSale(SaleRequestDto req, UUID requesterUserId) {
        // Validar branch
        Branch branch = branchRepository.findById(req.getBranchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal no encontrada"));

        // Validar usuario solicitante (resuelto desde JWT)
        User user = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario solicitante no encontrado"));

        // Preparar entidad Sale
        Sale sale = new Sale();
        sale.setBranch(branch);
        sale.setUser(user);
        sale.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        List<SaleItemResponseDto> responseItems = new ArrayList<>();
        List<SaleDetail> detailsToSave = new ArrayList<>();
        List<InventoryTransaction> txToSave = new ArrayList<>();

        // Procesar items
        for (SaleItemRequestDto item : req.getItems()) {
            // Validar producto
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + item.getProductId()));

            // Precio unitario: usar price si se suministra, si no usar salePrice de inventory
            BigDecimal unitPrice = item.getPrice();
            if (unitPrice == null) {
                // intentar obtener inventory para leer salePrice
                Optional<Inventory> invOpt = inventoryRepository.findByBranchIdAndProductId(branch.getId(), product.getId());
                if (invOpt.isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no disponible en la sucursal");
                }
                Inventory inv = invOpt.get();
                unitPrice = inv.getSalePrice() != null ? inv.getSalePrice() : BigDecimal.ZERO;
            }

            // Intentar decrementar stock atómicamente
            int updated = inventoryRepository.decrementQuantity(branch.getId(), product.getId(), item.getQuantity());
            if (updated == 0) {
                throw new InsufficientStockException("Stock insuficiente para producto: " + product.getId());
            }

            // Crear SaleDetail
            SaleDetail sd = new SaleDetail();
            sd.setSale(sale);
            sd.setProduct(product);
            sd.setQuantity(item.getQuantity());
            sd.setPrice(unitPrice.setScale(2, RoundingMode.HALF_UP));
            detailsToSave.add(sd);

            // calcular line total
            BigDecimal discount = item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO;
            BigDecimal lineTotal = unitPrice.multiply(item.getQuantity()).subtract(discount != null ? discount : BigDecimal.ZERO);
            total = total.add(lineTotal);

            // Añadir a lista de respuesta
            SaleItemResponseDto sir = new SaleItemResponseDto();
            sir.setProductId(product.getId());
            sir.setProductName(product.getName());
            sir.setQuantity(item.getQuantity());
            sir.setPrice(unitPrice.setScale(2, RoundingMode.HALF_UP));
            sir.setDiscount(discount);
            sir.setLineTotal(lineTotal.setScale(2, RoundingMode.HALF_UP));
            responseItems.add(sir);

            // Registrar inventory transaction
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(product);
            tx.setBranch(branch);
            tx.setUser(user);
            tx.setType("OUT");
            tx.setQuantity(item.getQuantity());
            tx.setReason("SALE");
            tx.setReferenceType("SALE");
            txToSave.add(tx);
        }

        // Aplicar descuento total si existe
        BigDecimal discountTotal = req.getDiscountTotal() != null ? req.getDiscountTotal() : BigDecimal.ZERO;
        BigDecimal finalTotal = total.subtract(discountTotal != null ? discountTotal : BigDecimal.ZERO);
        sale.setTotal(finalTotal.setScale(2, RoundingMode.HALF_UP));

        // Generar receipt simple
        String receipt = "S-" + DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now()) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        sale.setCreatedAt(Instant.now());

        // Guardar sale
        Sale saved = saleRepository.save(sale);

        // Guardar detalles y asignar sale
        for (SaleDetail sd : detailsToSave) {
            sd.setSale(saved);
            saleDetailRepository.save(sd);
        }

        // Guardar transacciones con referenceId
        for (InventoryTransaction tx : txToSave) {
            tx.setReferenceId(saved.getId());
            inventoryTransactionRepository.save(tx);
        }

        // Construir DTO de respuesta
        SaleResponseDto resp = new SaleResponseDto();
        resp.setId(saved.getId());
        resp.setReceiptNumber(receipt);
        resp.setStatus("COMPLETED");
        resp.setBranchId(branch.getId());
        resp.setUserId(user.getId());
        resp.setTotal(saved.getTotal());
        resp.setDiscountTotal(discountTotal);
        resp.setCreatedAt(saved.getCreatedAt());
        resp.setItems(responseItems);

        // Publicar evento para chequear stock después de commit
        try {
            eventPublisher.publishEvent(new com.camilocuenca.inventorysystem.events.LowStockCheckEvent(branch.getId(), user.getId()));
        } catch (Exception e) {
            // no fallar la transacción por problemas de publicación de evento
        }

        return resp;
    }
}
