package com.camilocuenca.inventorysystem.dto.transfer;

import java.time.Instant;

/**
 * DTO para reportar cumplimiento logístico (estimado vs real).
 */
public class TransferComplianceDto {
    private String transferId;
    private Instant estimatedArrival;
    private Instant receivedAt;
    private Long diffMinutes; // receivedAt - estimatedArrival en minutos (positivo si llegó después)
    private boolean met; // true si receivedAt <= estimatedArrival

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public Instant getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(Instant estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Long getDiffMinutes() {
        return diffMinutes;
    }

    public void setDiffMinutes(Long diffMinutes) {
        this.diffMinutes = diffMinutes;
    }

    public boolean isMet() {
        return met;
    }

    public void setMet(boolean met) {
        this.met = met;
    }
}

