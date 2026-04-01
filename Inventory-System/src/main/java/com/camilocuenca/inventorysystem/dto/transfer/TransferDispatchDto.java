package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class TransferDispatchDto {

    @NotBlank(message = "carrier es requerido")
    private String carrier;

    @NotNull(message = "estimatedArrival es requerido")
    private Instant estimatedArrival;

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public Instant getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(Instant estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }
}

