package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.transfer.*;
import com.camilocuenca.inventorysystem.dto.transfer.TransferReceiveDto;
import com.camilocuenca.inventorysystem.model.*;
import com.camilocuenca.inventorysystem.model.TransferAlert;
import com.camilocuenca.inventorysystem.repository.*;
import com.camilocuenca.inventorysystem.repository.TransferAlertRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.TransferService;
import com.camilocuenca.inventorysystem.exceptions.InsufficientStockException;
import com.camilocuenca.inventorysystem.Enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final TransferDetailRepository transferDetailRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final TransferAlertRepository transferAlertRepository;

    @Autowired
    public TransferServiceImpl(TransferRepository transferRepository,
                               TransferDetailRepository transferDetailRepository,
                               BranchRepository branchRepository,
                               ProductRepository productRepository,
                               UserRepository userRepository,
                               InventoryRepository inventoryRepository,
                               InventoryTransactionRepository inventoryTransactionRepository,
                               TransferAlertRepository transferAlertRepository) {
        this.transferRepository = transferRepository;
        this.transferDetailRepository = transferDetailRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.transferAlertRepository = transferAlertRepository;
    }

    /**
     * Crea la solicitud de transferencia (estado PENDING).
     *
     * Comportamiento híbrido de autorización/origen:
     * - Si el usuario solicitante es ADMIN: se utiliza el campo originBranchId que venga en el cuerpo (req).
     * - Si el usuario solicitante NO es ADMIN: se requiere que el usuario pertenezca a la sucursal destino
     *   ya que, por seguridad, sólo la sucursal destino (o un ADMIN) puede generar la solicitud; en ese caso
     *   el originBranchId proporcionado en el body será validado pero la autorización principal se basa en
     *   la pertenencia del usuario a la sucursal destino.
     *
     * Esto evita que un operador no autorizado cree solicitudes en nombre de otras sucursales, pero permite
     * que la sucursal destino solicite stock a cualquier origen disponible.
     */
    @Override
    @Transactional
    public TransferResponseDto requestTransfer(TransferRequestDto req, UUID requesterUserId) {
        // Validar usuario solicitante (debe existir)
        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario solicitante no encontrado"));

        boolean isAdminRequester = requester.getRole() != null && Role.ADMIN.equals(requester.getRole());

        // Origin: siempre viene en el body (admin o manager)
        Branch origin = branchRepository.findById(req.getOriginBranchId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal origen no encontrada"));

        // Destination: si es ADMIN, debe venir en el body; si no, se toma de la sucursal del requester
        Branch destination;
        if (isAdminRequester) {
            if (req.getDestinationBranchId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "destinationBranchId es requerido para administradores");
            }
            destination = branchRepository.findById(req.getDestinationBranchId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sucursal destino no encontrada"));
        } else {
            // Para managers/operadores: usar la sucursal del usuario
            if (requester.getBranch() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no está asignado a ninguna sucursal");
            }
            destination = requester.getBranch();
        }

        // Validar productos
        Map<UUID, Product> products = new HashMap<>();
        for (TransferItemRequestDto it : req.getItems()) {
            Product p = productRepository.findById(it.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + it.getProductId()));
            products.put(p.getId(), p);
        }

        // Permisos: si no es ADMIN, el usuario debe pertenecer a la sucursal destino (ya es así porque destination = requester.branch)
        if (!isAdminRequester) {
            if (requester.getBranch() == null || !requester.getBranch().getId().equals(destination.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el usuario de la sucursal destino o un ADMIN puede crear la solicitud de transferencia");
            }
        }

        // Crear entidad Transfer
        Transfer t = new Transfer();
        t.setOriginBranch(origin);
        t.setDestinationBranch(destination);
        t.setStatus("PENDING");
        t.setCreatedAt(Instant.now());
        t.setCreatedBy(requester);

        Transfer saved = transferRepository.save(t);

        // Crear detalles
        List<TransferDetail> details = new ArrayList<>();
        for (TransferItemRequestDto it : req.getItems()) {
            TransferDetail td = new TransferDetail();
            td.setTransfer(saved);
            td.setProduct(products.get(it.getProductId()));
            td.setQuantity(it.getQuantity());
            td.setReceivedQuantity(BigDecimal.ZERO);
            details.add(td);
        }
        // Guardar todos los detalles en lote
        transferDetailRepository.saveAll(details);

        // Construir DTO respuesta
        TransferResponseDto resp = new TransferResponseDto();
        resp.setId(saved.getId());
        resp.setOriginBranchId(origin.getId());
        resp.setDestinationBranchId(destination.getId());
        resp.setStatus(saved.getStatus());
        resp.setCreatedAt(saved.getCreatedAt());
        resp.setItems(details.stream().map(d -> {
            TransferDetailResponseDto dr = new TransferDetailResponseDto();
            dr.setProductId(d.getProduct().getId());
            dr.setProductName(d.getProduct().getName());
            dr.setQuantityRequested(d.getQuantity());
            dr.setQuantityConfirmed(BigDecimal.ZERO);
            dr.setReceivedQuantity(d.getReceivedQuantity());
            return dr;
        }).collect(Collectors.toList()));

        return resp;
    }

    /**
     * Preparación/confirmación por la sucursal origen: validar stock y decrementar; actualizar estado y registrar tx.
     */
    @Override
    @Transactional
    public TransferResponseDto prepareTransfer(UUID transferId, TransferPrepareDto body, UUID requesterUserId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transferencia no encontrada"));

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Verificar que requester pertenece a origin branch o es admin role
        boolean isOriginUser = requester.getBranch() != null && requester.getBranch().getId().equals(transfer.getOriginBranch().getId());
        boolean isAdmin = requester.getRole() != null && (Role.ADMIN.equals(requester.getRole()));
        if (!isOriginUser && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado para preparar esta transferencia");
        }

        // Mapear detalles existentes
        List<TransferDetail> existingDetails = transferDetailRepository.findByTransferId(transferId);
        Map<UUID, TransferDetail> detailByProduct = existingDetails.stream().collect(Collectors.toMap(d -> d.getProduct().getId(), d -> d));

        List<InventoryTransaction> txs = new ArrayList<>();

        for (TransferPrepareItemDto pit : body.getItems()) {
            TransferDetail td = detailByProduct.get(pit.getProductId());
            if (td == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no está en la transferencia: " + pit.getProductId());
            }

            BigDecimal toSend = pit.getQuantityConfirmed();
            if (toSend.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityConfirmed debe ser mayor que cero para producto: " + pit.getProductId());
            }

            // Validar inventario en origin
            Optional<Inventory> invOpt = inventoryRepository.findByBranchIdAndProductId(transfer.getOriginBranch().getId(), pit.getProductId());
            if (invOpt.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Producto no tiene inventario en la sucursal origen: " + pit.getProductId());
            }

            // Intentar decrementar atómicamente
            int updated = inventoryRepository.decrementQuantity(transfer.getOriginBranch().getId(), pit.getProductId(), toSend);
            if (updated == 0) {
                throw new InsufficientStockException("Stock insuficiente para producto: " + pit.getProductId());
            }

            // Actualizar detalle confirmado
            td.setQuantityConfirmed(toSend);
            transferDetailRepository.save(td);

            // Registrar transaction OUT en origin
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(td.getProduct());
            tx.setBranch(transfer.getOriginBranch());
            tx.setUser(requester);
            tx.setType("OUT");
            tx.setQuantity(toSend);
            tx.setReason("TRANSFER_OUT");
            tx.setReferenceType("TRANSFER");
            tx.setReferenceId(transfer.getId());
            txs.add(tx);
        }

        // Persistir transacciones
        if (!txs.isEmpty()) {
            inventoryTransactionRepository.saveAll(txs);
        }

        // Actualizar estado y shippedAt
        transfer.setStatus("SHIPPED");
        transfer.setShippedAt(Instant.now());
        transfer.setApprovedBy(requester);
        transferRepository.save(transfer);

        // Construir respuesta
        TransferResponseDto resp = new TransferResponseDto();
        resp.setId(transfer.getId());
        resp.setOriginBranchId(transfer.getOriginBranch().getId());
        resp.setDestinationBranchId(transfer.getDestinationBranch().getId());
        resp.setStatus(transfer.getStatus());
        resp.setCreatedAt(transfer.getCreatedAt());
        resp.setShippedAt(transfer.getShippedAt());
        List<TransferDetailResponseDto> items = existingDetails.stream().map(d -> {
            TransferDetailResponseDto dr = new TransferDetailResponseDto();
            dr.setProductId(d.getProduct().getId());
            dr.setProductName(d.getProduct().getName());
            dr.setQuantityRequested(d.getQuantity());
            dr.setQuantityConfirmed(d.getQuantityConfirmed());
            dr.setReceivedQuantity(d.getReceivedQuantity());
            return dr;
        }).collect(Collectors.toList());
        resp.setItems(items);
        return resp;
    }

    @Override
    @Transactional
    public TransferResponseDto dispatchTransfer(UUID transferId, TransferDispatchDto body, UUID requesterUserId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transferencia no encontrada"));

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Solo origin branch o admin puede registrar el despacho
        boolean isOriginUser = requester.getBranch() != null && requester.getBranch().getId().equals(transfer.getOriginBranch().getId());
        boolean isAdmin = requester.getRole() != null && Role.ADMIN.equals(requester.getRole());
        if (!isOriginUser && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado para registrar el despacho");
        }

        transfer.setCarrier(body.getCarrier());
        transfer.setEstimatedArrival(body.getEstimatedArrival());
        transfer.setStatus("IN_TRANSIT");
        transfer.setShippedAt(Instant.now());
        transferRepository.save(transfer);

        TransferResponseDto resp = new TransferResponseDto();
        resp.setId(transfer.getId());
        resp.setStatus(transfer.getStatus());
        resp.setOriginBranchId(transfer.getOriginBranch().getId());
        resp.setDestinationBranchId(transfer.getDestinationBranch().getId());
        resp.setShippedAt(transfer.getShippedAt());
        resp.setCreatedAt(transfer.getCreatedAt());
        return resp;
    }

    @Override
    @Transactional
    public TransferResponseDto receiveTransfer(UUID transferId, TransferReceiveDto body, UUID requesterUserId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transferencia no encontrada"));

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Solo destination branch o admin puede confirmar recepción
        boolean isDestinationUser = requester.getBranch() != null && requester.getBranch().getId().equals(transfer.getDestinationBranch().getId());
        boolean isAdmin = requester.getRole() != null && Role.ADMIN.equals(requester.getRole());
        if (!isDestinationUser && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado para confirmar la recepción");
        }

        // Mapear detalles por product
        List<TransferDetail> details = transferDetailRepository.findByTransferId(transferId);
        Map<UUID, TransferDetail> detailByProduct = details.stream().collect(Collectors.toMap(d -> d.getProduct().getId(), d -> d));

        List<InventoryTransaction> transactions = new ArrayList<>();
        List<TransferAlert> alerts = new ArrayList<>();

        boolean anyMissing = false;

        for (TransferReceiveDto.TransferReceiveItemDto it : body.getItems()) {
            TransferDetail td = detailByProduct.get(it.getProductId());
            if (td == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no pertenece a la transferencia: " + it.getProductId());

            java.math.BigDecimal received = it.getReceivedQuantity();
            java.math.BigDecimal confirmed = td.getQuantityConfirmed() != null ? td.getQuantityConfirmed() : td.getQuantity();

            // Si recibido < confirmado -> hay faltantes
            if (received.compareTo(confirmed) < 0) {
                java.math.BigDecimal missing = confirmed.subtract(received);
                anyMissing = true;

                TransferAlert alert = new TransferAlert();
                alert.setTransfer(transfer);
                alert.setProductId(it.getProductId());
                alert.setMissingQuantity(missing);
                alert.setCreatedAt(Instant.now());
                alert.setStatus("OPEN");
                alerts.add(alert);
            }

            // Incrementar inventario en destination (atómico)
            int updated = inventoryRepository.incrementQuantity(transfer.getDestinationBranch().getId(), it.getProductId(), received);
            if (updated == 0) {
                // No existe inventario para este producto en destino: crear uno
                Inventory inv = new Inventory();
                inv.setBranch(transfer.getDestinationBranch());
                Product p = productRepository.findById(it.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + it.getProductId()));
                inv.setProduct(p);
                inv.setQuantity(received);
                inv.setUpdatedAt(Instant.now());
                // save
                inventoryRepository.save(inv);
            }

            // Registrar transaction IN en destino
            InventoryTransaction tx = new InventoryTransaction();
            tx.setProduct(td.getProduct());
            tx.setBranch(transfer.getDestinationBranch());
            tx.setUser(requester);
            tx.setType("IN");
            tx.setQuantity(received);
            tx.setReason("TRANSFER_IN");
            tx.setReferenceType("TRANSFER");
            tx.setReferenceId(transfer.getId());
            transactions.add(tx);

            // Actualizar receivedQuantity en detalle
            td.setReceivedQuantity(received);
            transferDetailRepository.save(td);
        }

        // Guardar alertas y transacciones
        if (!alerts.isEmpty()) {
            transferAlertRepository.saveAll(alerts);
        }
        if (!transactions.isEmpty()) {
            inventoryTransactionRepository.saveAll(transactions);
        }

        // Actualizar estado
        transfer.setReceivedAt(Instant.now());
        transfer.setStatus(anyMissing ? "PARTIALLY_RECEIVED" : "RECEIVED");
        transferRepository.save(transfer);

        // Construir respuesta
        TransferResponseDto resp = new TransferResponseDto();
        resp.setId(transfer.getId());
        resp.setStatus(transfer.getStatus());
        resp.setOriginBranchId(transfer.getOriginBranch().getId());
        resp.setDestinationBranchId(transfer.getDestinationBranch().getId());
        resp.setReceivedAt(transfer.getReceivedAt());
        resp.setCreatedAt(transfer.getCreatedAt());
        return resp;
    }
}
