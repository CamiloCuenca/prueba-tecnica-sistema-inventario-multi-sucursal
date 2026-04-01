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
     *    Crea la solicitud de transferencia (estado PENDING).
     *
     *       Comportamiento híbrido de autorización/origen:
     *       - Si el usuario solicitante es ADMIN: se utiliza el campo originBranchId que venga en el cuerpo (req).
     *       - Si el usuario solicitante NO es ADMIN: se requiere que el usuario pertenezca a la sucursal destino
     *         ya que, por seguridad, sólo la sucursal destino (o un ADMIN) puede generar la solicitud; en ese caso
     *         el originBranchId proporcionado en el body será validado pero la autorización principal se basa en
     *         la pertenencia del usuario a la sucursal destino.
     *
     *       Esto evita que un operador no autorizado cree solicitudes en nombre de otras sucursales, pero permite
     *       que la sucursal destino solicite stock a cualquier origen disponible.
     *
     * @param req DTO con la información de la solicitud (originBranchId, destinationBranchId, items)
     * @param requesterUserId UUID del usuario que realiza la solicitud (resuelto desde el JWT)
     * @return
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

        // Validar que origin y destination sean distintas
        if (origin.getId().equals(destination.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursal origen y destino deben ser distintas");
        }

        // Validar productos y unicidad en el request (no permitir productos duplicados en la misma solicitud)
        Set<UUID> seenProductIds = new HashSet<>();
        List<UUID> duplicateIds = new ArrayList<>();
        for (TransferItemRequestDto it : req.getItems()) {
            if (!seenProductIds.add(it.getProductId())) {
                duplicateIds.add(it.getProductId());
            }
        }
        if (!duplicateIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Productos duplicados en la solicitud: " + duplicateIds);
        }

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
            // quantityConfirmed se deja null para distinguir "no preparado" de "confirmado con 0"
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
        resp.setShippedAt(saved.getShippedAt());
        resp.setDispatchedAt(saved.getDispatchedAt());
        resp.setCarrier(saved.getCarrier());
        resp.setEstimatedArrival(saved.getEstimatedArrival());
        resp.setItems(details.stream().map(d -> {
            TransferDetailResponseDto dr = new TransferDetailResponseDto();
            dr.setProductId(d.getProduct().getId());
            dr.setProductName(d.getProduct().getName());
            dr.setQuantityRequested(d.getQuantity());
            dr.setQuantityConfirmed(d.getQuantityConfirmed());
            dr.setReceivedQuantity(d.getReceivedQuantity());
            return dr;
        }).collect(Collectors.toList()));

        return resp;
    }

       /**
     * Preparación / confirmación de la transferencia por la sucursal origen.
     * La sucursal origen revisa disponibilidad y confirma o ajusta la cantidad que se envi
     * @param transferId id de la transferencia a preparar
     * @param body DTO con las cantidades confirmadas por producto
     * @param requesterUserId UUID del usuario que confirma (resuelto desde JWT)
     * @return
     */
    @Override
    @Transactional
    public TransferResponseDto prepareTransfer(UUID transferId, TransferPrepareDto body, UUID requesterUserId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transferencia no encontrada"));

        User requester = userRepository.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Validar que se envíe al menos un item en el body
        if (body.getItems() == null || body.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere al menos un item para preparar la transferencia");
        }

        // Verificar que requester pertenece a origin branch o es admin role
        boolean isOriginUser = requester.getBranch() != null && requester.getBranch().getId().equals(transfer.getOriginBranch().getId());
        boolean isAdmin = requester.getRole() != null && (Role.ADMIN.equals(requester.getRole()));
        if (!isOriginUser && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario no autorizado para preparar esta transferencia");
        }

        // Mapear detalles existentes
        List<TransferDetail> existingDetails = transferDetailRepository.findByTransferId(transferId);
        Map<UUID, TransferDetail> detailByProduct = existingDetails.stream().collect(Collectors.toMap(d -> d.getProduct().getId(), d -> d, (a, b) -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detalle de transferencia duplicado para el producto: " + a.getProduct().getId());
        }));

        List<InventoryTransaction> txs = new ArrayList<>();

        for (TransferPrepareItemDto pit : body.getItems()) {
            TransferDetail td = detailByProduct.get(pit.getProductId());
            if (td == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no está en la transferencia: " + pit.getProductId());
            }

            BigDecimal toSend = pit.getQuantityConfirmed();
            if (toSend == null || toSend.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityConfirmed debe ser mayor que cero para producto: " + pit.getProductId());
            }

            // VALIDACIÓN ADICIONAL: no permitir confirmar más de lo solicitado
            if (td.getQuantity() != null && toSend.compareTo(td.getQuantity()) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityConfirmed no puede ser mayor que la cantidad solicitada para el producto: " + pit.getProductId());
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

        // Determinar si todos los detalles han sido preparados
        boolean allPrepared = existingDetails.stream().allMatch(d -> d.getQuantityConfirmed() != null && d.getQuantityConfirmed().compareTo(BigDecimal.ZERO) > 0);
        boolean anyProcessed = !txs.isEmpty();

        if (!anyProcessed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se procesó ningún item en la preparación");
        }

        if (allPrepared) {
            transfer.setStatus("SHIPPED");
            // Si quedó completamente preparado, marcar fecha de envío
            transfer.setShippedAt(Instant.now());
        } else {
            transfer.setStatus("PARTIALLY_SHIPPED");
            // No setear shippedAt para envíos parciales aquí: el envío físico (dispatch) lo marcará
        }

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
        resp.setDispatchedAt(transfer.getDispatchedAt());
        resp.setCarrier(transfer.getCarrier());
        resp.setEstimatedArrival(transfer.getEstimatedArrival());
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
        // Registrar metadata de ruta si viene
        if (body.getRouteId() != null) transfer.setRouteId(body.getRouteId());
        if (body.getRoutePriority() != null) {
            try {
                transfer.setRoutePriority(com.camilocuenca.inventorysystem.Enums.RoutePriority.valueOf(body.getRoutePriority()));
            } catch (IllegalArgumentException ignored) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "routePriority inválido. Valores permitidos: LOW, MEDIUM, HIGH, URGENT");
            }
        }
        if (body.getEstimatedTransitMinutes() != null) transfer.setEstimatedTransitMinutes(body.getEstimatedTransitMinutes());
        if (body.getRouteCost() != null) transfer.setRouteCost(body.getRouteCost());
        transfer.setStatus("IN_TRANSIT");
        // No sobrescribir shippedAt (fecha de preparación). Registrar la fecha real de despacho en dispatchedAt si no existe.
        if (transfer.getDispatchedAt() == null) {
            transfer.setDispatchedAt(Instant.now());
        }
        transferRepository.save(transfer);

        TransferResponseDto resp = new TransferResponseDto();
        resp.setId(transfer.getId());
        resp.setStatus(transfer.getStatus());
        resp.setOriginBranchId(transfer.getOriginBranch().getId());
        resp.setDestinationBranchId(transfer.getDestinationBranch().getId());
        resp.setShippedAt(transfer.getShippedAt());
        resp.setCreatedAt(transfer.getCreatedAt());
        resp.setDispatchedAt(transfer.getDispatchedAt());
        resp.setCarrier(transfer.getCarrier());
        resp.setEstimatedArrival(transfer.getEstimatedArrival());

        // Calcular minutos de tránsito estimados si no se proporcionaron
        if (body.getEstimatedTransitMinutes() == null && transfer.getRoutePriority() != null) {
            Double originLat = transfer.getOriginBranch().getLatitude();
            Double originLon = transfer.getOriginBranch().getLongitude();
            Double destLat = transfer.getDestinationBranch().getLatitude();
            Double destLon = transfer.getDestinationBranch().getLongitude();
            double distanceKm = haversineKm(originLat, originLon, destLat, destLon);
            int estimatedMinutes = estimateMinutesFromDistanceKm(distanceKm, transfer.getRoutePriority());
            resp.setEstimatedTransitMinutes(estimatedMinutes);
        }

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

        // Mapear detalles por producto (controlando duplicados en DB)
        List<TransferDetail> details = transferDetailRepository.findByTransferId(transferId);
        Map<UUID, TransferDetail> detailByProduct = details.stream().collect(Collectors.toMap(d -> d.getProduct().getId(), d -> d, (a, b) -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Detalle de transferencia duplicado para el producto: " + a.getProduct().getId());
        }));

        if (body.getItems() == null || body.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere al menos un item para confirmar la recepción");
        }

        List<InventoryTransaction> transactions = new ArrayList<>();
        List<TransferAlert> alerts = new ArrayList<>();

        // Procesar sólo los items enviados, pero siempre calcularemos el estado global considerando todos los detalles
        for (TransferReceiveDto.TransferReceiveItemDto it : body.getItems()) {
            TransferDetail td = detailByProduct.get(it.getProductId());
            if (td == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto no pertenece a la transferencia: " + it.getProductId());
            }

            java.math.BigDecimal received = it.getReceivedQuantity();
            if (received == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receivedQuantity es requerido para el producto: " + it.getProductId());
            }
            // Validación: received > 0
            if (received.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receivedQuantity debe ser mayor que cero para el producto: " + it.getProductId());
            }

            java.math.BigDecimal confirmed = td.getQuantityConfirmed() != null ? td.getQuantityConfirmed() : td.getQuantity();
            if (confirmed == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cantidad confirmada/solicitada no está definida para el producto: " + it.getProductId());
            }
            // Validación: no permitir received > confirmed
            if (received.compareTo(confirmed) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receivedQuantity no puede ser mayor que la cantidad confirmada/solicitada para el producto: " + it.getProductId());
            }

            // Evitar disminuir la cantidad ya registrada: solo permitir received >= existingReceived
            java.math.BigDecimal existingReceived = td.getReceivedQuantity() != null ? td.getReceivedQuantity() : java.math.BigDecimal.ZERO;
            if (received.compareTo(existingReceived) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receivedQuantity no puede ser menor que la cantidad ya registrada para el producto: " + it.getProductId());
            }

            // Calcular incremento efectivo (delta) para evitar duplicar incrementos en múltiples llamadas
            java.math.BigDecimal delta = received.subtract(existingReceived);
            if (delta.compareTo(java.math.BigDecimal.ZERO) > 0) {
                // Incrementar inventario en destination (atómico) sólo con el delta
                int updated = inventoryRepository.incrementQuantity(transfer.getDestinationBranch().getId(), it.getProductId(), delta);
                if (updated == 0) {
                    // No existe inventario para este producto en destino: crear uno con la cantidad delta
                    Inventory inv = new Inventory();
                    inv.setBranch(transfer.getDestinationBranch());
                    Product p = productRepository.findById(it.getProductId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + it.getProductId()));
                    inv.setProduct(p);
                    inv.setQuantity(delta);
                    inv.setUpdatedAt(Instant.now());
                    inventoryRepository.save(inv);
                }

                // Registrar transaction IN en destino correspondiente al delta
                InventoryTransaction tx = new InventoryTransaction();
                tx.setProduct(td.getProduct());
                tx.setBranch(transfer.getDestinationBranch());
                tx.setUser(requester);
                tx.setType("IN");
                tx.setQuantity(delta);
                tx.setReason("TRANSFER_IN");
                tx.setReferenceType("TRANSFER");
                tx.setReferenceId(transfer.getId());
                transactions.add(tx);
            }

            // Actualizar receivedQuantity en detalle al valor total recibido
            td.setReceivedQuantity(received);
            transferDetailRepository.save(td);

            // Si received < confirmed, generar alerta (se registra la diferencia actual)
            if (received.compareTo(confirmed) < 0) {
                java.math.BigDecimal missing = confirmed.subtract(received);
                TransferAlert alert = new TransferAlert();
                alert.setTransfer(transfer);
                alert.setProductId(it.getProductId());
                alert.setMissingQuantity(missing);
                alert.setCreatedAt(Instant.now());
                alert.setStatus("OPEN");
                alerts.add(alert);
            }
        }

        // Persistir transacciones y alertas generadas en esta llamada
        if (!transactions.isEmpty()) {
            inventoryTransactionRepository.saveAll(transactions);
        }
        if (!alerts.isEmpty()) {
            transferAlertRepository.saveAll(alerts);
        }

        // Determinar estado global considerando todas las líneas del transfer
        boolean anyReceived = false;
        boolean anyMissingOverall = false;
        boolean allFullyReceived = true;
        for (TransferDetail td : details) {
            java.math.BigDecimal conf = td.getQuantityConfirmed() != null ? td.getQuantityConfirmed() : td.getQuantity();
            java.math.BigDecimal rec = td.getReceivedQuantity() != null ? td.getReceivedQuantity() : java.math.BigDecimal.ZERO;

            if (rec.compareTo(java.math.BigDecimal.ZERO) > 0) anyReceived = true;
            if (conf == null) conf = java.math.BigDecimal.ZERO;
            if (rec.compareTo(conf) < 0) {
                anyMissingOverall = true;
                allFullyReceived = false;
            }
        }

        if (anyReceived) {
            if (allFullyReceived) {
                transfer.setStatus("RECEIVED");
            } else {
                transfer.setStatus("PARTIALLY_RECEIVED");
            }
            transfer.setReceivedAt(Instant.now());
            // Calcular minutos reales de tránsito si dispatchedAt existe
            if (transfer.getDispatchedAt() != null) {
                long minutes = java.time.Duration.between(transfer.getDispatchedAt(), Instant.now()).toMinutes();
                transfer.setActualTransitMinutes((int) Math.max(0, minutes));
            }
            transferRepository.save(transfer);
        }

        // Construir respuesta
        TransferResponseDto resp = new TransferResponseDto();
        resp.setId(transfer.getId());
        resp.setStatus(transfer.getStatus());
        resp.setOriginBranchId(transfer.getOriginBranch().getId());
        resp.setDestinationBranchId(transfer.getDestinationBranch().getId());
        resp.setReceivedAt(transfer.getReceivedAt());
        resp.setCreatedAt(transfer.getCreatedAt());
        resp.setCarrier(transfer.getCarrier());
        resp.setEstimatedArrival(transfer.getEstimatedArrival());
        return resp;
    }

    private double haversineKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return -1d;
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private int estimateMinutesFromDistanceKm(double km, com.camilocuenca.inventorysystem.Enums.RoutePriority priority) {
        // Velocidades media estimadas por prioridad (km/h)
        double speedKmh = 50; // default
        if (priority == null) priority = com.camilocuenca.inventorysystem.Enums.RoutePriority.MEDIUM;
        switch (priority) {
            case URGENT:
                speedKmh = 80; break;
            case HIGH:
                speedKmh = 60; break;
            case MEDIUM:
                speedKmh = 45; break;
            case LOW:
                speedKmh = 30; break;
        }
        if (km < 0) return -1;
        double hours = km / speedKmh;
        return (int) Math.max(1, Math.round(hours * 60));
    }
}
