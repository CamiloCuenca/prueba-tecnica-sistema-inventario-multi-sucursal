package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.transfer.TransferPrepareDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferRequestDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferResponseDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferDispatchDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferReceiveDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferListDto;
import com.camilocuenca.inventorysystem.dto.transfer.TransferComplianceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.camilocuenca.inventorysystem.Enums.TransferStatus;

import java.util.UUID;

/**
 * Interfaz del servicio de Transferencias.
 *
 * Define operaciones para gestionar el ciclo de vida de una transferencia entre sucursales,
 * incluyendo la solicitud por la sucursal destino y la preparación/confirmación por la sucursal origen.
 */
public interface TransferService {

    /**
     * Solicita una transferencia desde una sucursal origen hacia una sucursal destino.
     * Este método crea la entidad Transfer en estado PENDING (o similar) y persistirá los detalles.
     * El usuario solicitante (resolved desde JWT) debe ser un administrador o un usuario de la sucursal destino.
     *
     * Contrato:
     * - Valida existencia de sucursales y productos.
     * - No reduce stock en origen todavía (eso ocurre en la preparación/envío).
     *
     * @param req DTO con la información de la solicitud (originBranchId, destinationBranchId, items)
     * @param requesterUserId UUID del usuario que realiza la solicitud (resuelto desde el JWT)
     * @return TransferResponseDto con la transferencia creada
     */
    TransferResponseDto requestTransfer(TransferRequestDto req, UUID requesterUserId);

    /**
     * Preparación / confirmación de la transferencia por la sucursal origen.
     * La sucursal origen revisa disponibilidad y confirma o ajusta la cantidad que se enviará.
     * Este método debe validar stock disponible y, si procede, bloquear o decrementar stock y
     * actualizar el estado de la transferencia (e.g., APPROVED/SHIPPED) y los detalles enviados.
     *
     * Contrato:
     * - Verifica que el usuario solicitante pertenece a la sucursal origen o tenga rol admin.
     * - Si la cantidad a enviar es menor que la solicitada, debe registrarse el ajuste en el detalle.
     * - Registra movimientos en inventory_transaction según corresponda.
     *
     * @param transferId id de la transferencia a preparar
     * @param body DTO con las cantidades confirmadas por producto
     * @param requesterUserId UUID del usuario que confirma (resuelto desde JWT)
     * @return TransferResponseDto con el estado actualizado de la transferencia
     */
    TransferResponseDto prepareTransfer(UUID transferId, TransferPrepareDto body, UUID requesterUserId);

    /**
     * Despacho de la transferencia por la sucursal origen. Este método se llama después de la preparación y confirma el envío.
     * @param transferId
     * @param body
     * @param requesterUserId
     * @return
     */
    TransferResponseDto dispatchTransfer(UUID transferId, TransferDispatchDto body, UUID requesterUserId);

    /**
     * Confirmar recepción (parcial o completa). Actualiza inventario de destino, crea alertas por faltantes.
     * @param transferId
     * @param body
     * @param requesterUserId
     * @return
     */
    TransferResponseDto receiveTransfer(UUID transferId, TransferReceiveDto body, UUID requesterUserId);

    /**
     * Lista paginada de transferencias activas (no cerradas) cuyo destino es la sucursal del usuario.
     * - Si el usuario es ADMIN y se provee branchId, se consultará para esa sucursal; en caso contrario
     *   se usará la sucursal del usuario autenticado.
     * - Devuelve una página de TransferListDto con información ligera para listados.
     *
     * @param requesterUserId UUID del usuario que realiza la consulta (resuelto desde JWT)
     * @param branchId (opcional) UUID de la sucursal a consultar (solo para ADMIN)
     * @param status (opcional) estado de la transferencia a filtrar
     * @param pageable paginación
     * @return página de TransferListDto
     */
    Page<TransferListDto> incomingTransfers(UUID requesterUserId, UUID branchId, TransferStatus status, Pageable pageable);

    /**
     * Lista paginada de transferencias activas (no cerradas) cuyo origen es la sucursal del usuario.
     * Comportamiento similar a incomingTransfers respecto a permisos y branchId.
     *
     * @param requesterUserId UUID del usuario que realiza la consulta (resuelto desde JWT)
     * @param branchId (opcional) UUID de la sucursal a consultar (solo para ADMIN)
     * @param status (opcional) estado de la transferencia a filtrar
     * @param pageable paginación
     * @return página de TransferListDto
     */
    Page<TransferListDto> outgoingTransfers(UUID requesterUserId, UUID branchId, TransferStatus status, Pageable pageable);

    /**
     * Obtiene el detalle completo de una transferencia por su id.
     * - Admin puede consultar cualquier transferencia.
     * - Managers/Operators sólo pueden consultar transfers donde su sucursal sea origen o destino.
     *
     * @param requesterUserId UUID del usuario que solicita (resuelto desde JWT)
     * @param transferId id de la transferencia
     * @return TransferResponseDto con el detalle completo
     */
    TransferResponseDto getTransferDetail(UUID requesterUserId, UUID transferId);

    /**
     * Calcula el cumplimiento logístico de la transferencia comparando estimatedArrival vs receivedAt.
     * @param requesterUserId UUID del usuario que solicita
     * @param transferId id de la transferencia
     * @return TransferComplianceDto con la diferencia en minutos y si se cumplió
     */
    TransferComplianceDto getLogisticsCompliance(UUID requesterUserId, UUID transferId);

}
