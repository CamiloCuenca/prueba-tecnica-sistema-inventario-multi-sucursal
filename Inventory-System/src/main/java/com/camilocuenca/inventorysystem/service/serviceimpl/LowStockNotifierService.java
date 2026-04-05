package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.dto.metrics.InventoryLowStockDto;
import com.camilocuenca.inventorysystem.dto.email.EmailDTO;
import com.camilocuenca.inventorysystem.model.StockAlert;
import com.camilocuenca.inventorysystem.model.User;
import com.camilocuenca.inventorysystem.Enums.Role;
import com.camilocuenca.inventorysystem.repository.StockAlertRepository;
import com.camilocuenca.inventorysystem.repository.UserRepository;
import com.camilocuenca.inventorysystem.repository.InventoryRepository;
import com.camilocuenca.inventorysystem.service.serviceInterface.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LowStockNotifierService {

    private static final Logger log = LoggerFactory.getLogger(LowStockNotifierService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockAlertRepository stockAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Notifica a los MANAGER y ADMIN de la sucursal sobre productos con stock ALTO o CRÍTICO.
     * Sólo notifica aquellos items que aún no fueron notificados hoy con la misma urgencia.
     *
     * @param branchId sucursal a revisar
     * @param actorId  UUID del usuario que dispara la notificación (puede ser null en jobs)
     */
    @Transactional
    public void notifyLowStock(UUID branchId, UUID actorId) {
        if (branchId == null) throw new IllegalArgumentException("branchId requerido");

        log.info("Iniciando notificación de stock bajo para branch={}", branchId);

        List<InventoryLowStockDto> lowStock = inventoryRepository.findLowStockAlertsByBranch(branchId);
        log.info("Encontrados {} items en estado bajo para branch={}", lowStock.size(), branchId);

        if (lowStock.isEmpty()) return;

        // Obtener destinatarios (MANAGER y ADMIN)
        List<Role> roles = new ArrayList<>();
        roles.add(Role.ADMIN);
        roles.add(Role.MANAGER);
        List<User> recipients = userRepository.findByBranchIdAndRoleIn(branchId, roles);

        if (recipients.isEmpty()) {
            log.warn("No hay usuarios MANAGER/ADMIN en branch={}", branchId);
            return;
        }

        // Para cada item, verificar si ya fue notificado hoy para la misma urgencia
        Instant startOfDay = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);

        for (InventoryLowStockDto item : lowStock) {
            String urgency = item.getUrgencyLevel();
            UUID productId = item.getProductId();

            List<StockAlert> already = stockAlertRepository.findByProductIdAndBranchIdAndUrgencyAndNotifiedAtAfter(productId, branchId, urgency, startOfDay);
            if (!already.isEmpty()) {
                // ya notificado hoy con la misma urgencia
                continue;
            }

            // Construir el cuerpo del email
            StringBuilder body = new StringBuilder();
            body.append("<p>Estimado equipo,</p>");
            body.append("<p>El producto <strong>").append(item.getProductName()).append("</strong> (SKU: ").append(item.getSku()).append(") en la sucursal presenta nivel de urgencia: <strong>").append(urgency).append("</strong>.</p>");
            body.append("<ul>");
            body.append("<li>Stock actual: ").append(item.getCurrentStock()).append("</li>");
            body.append("<li>Stock mínimo: ").append(item.getMinStock()).append("</li>");
            body.append("<li>Diferencia: ").append(item.getDifference()).append("</li>");
            body.append("</ul>");
            body.append("<p>Por favor coordine el reabastecimiento si aplica.</p>");
            body.append("<p>Atte.<br/>Sistema de Inventario</p>");

            String subject = "Alerta stock " + urgency + " - " + item.getProductName();

            // Enviar a cada destinatario
            for (User u : recipients) {
                try {
                    emailService.sendMail(new EmailDTO(u.getEmail(), subject, body.toString()));
                } catch (Exception e) {
                    log.error("Error enviando email a {}: {}", u.getEmail(), e.getMessage());
                }
            }

            // Persistir registro de alerta
            StockAlert sa = new StockAlert();
            sa.setBranchId(branchId);
            sa.setProductId(productId);
            sa.setUrgency(urgency);
            sa.setNotifiedAt(Instant.now());
            sa.setCreatedBy(actorId);
            stockAlertRepository.save(sa);
        }

        log.info("Finalizó notificación de stock bajo para branch={}", branchId);
    }
}

