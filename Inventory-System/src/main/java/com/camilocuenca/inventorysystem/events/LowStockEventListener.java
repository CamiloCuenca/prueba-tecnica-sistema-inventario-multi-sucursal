package com.camilocuenca.inventorysystem.events;

import com.camilocuenca.inventorysystem.service.serviceimpl.LowStockNotifierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class LowStockEventListener {
    private static final Logger log = LoggerFactory.getLogger(LowStockEventListener.class);

    @Autowired
    private LowStockNotifierService notifier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStockCheck(LowStockCheckEvent event) {
        try {
            log.info("Handling LowStockCheckEvent for branch={}", event.getBranchId());
            notifier.notifyLowStock(event.getBranchId(), event.getActorId());
        } catch (Exception e) {
            log.error("Error while handling LowStockCheckEvent: {}", e.getMessage(), e);
        }
    }
}

