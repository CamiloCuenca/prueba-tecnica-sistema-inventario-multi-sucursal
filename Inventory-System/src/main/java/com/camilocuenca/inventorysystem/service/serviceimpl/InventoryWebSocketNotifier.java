package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.websocket.InventoryUpdateMessageDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
public class InventoryWebSocketNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    public InventoryWebSocketNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Notifica una actualización de inventario a /topic/inventory-updates.{branchId}
     * Si branchId es null envía a /topic/inventory-updates.global
     */
    public void notifyInventoryUpdate(UUID branchId, UUID productId, Integer currentStock) {
        InventoryUpdateMessageDto msg = new InventoryUpdateMessageDto(branchId, productId, currentStock, Instant.now());
        try {
            String destination;
            if (branchId == null) {
                destination = "/topic/inventory-updates.global";
            } else {
                destination = String.format("/topic/inventory-updates.%s", branchId);
            }
            messagingTemplate.convertAndSend(destination, msg);
        } catch (Exception ex) {
            // no lanzar excepciones desde el notifier para no alterar flujo de negocio
            // registrar en logs si se requiere
        }
    }
}
