package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.math.BigDecimal;

public class TransferDispatchDto {

    @NotBlank(message = "carrier es requerido")
    private String carrier;

    @NotNull(message = "estimatedArrival es requerido")
    private Instant estimatedArrival;

    // Logistics optional
    private String routeId;
    private String routePriority; // LOW, MEDIUM, HIGH, URGENT
    private BigDecimal routeCost;

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

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRoutePriority() {
        return routePriority;
    }

    public void setRoutePriority(String routePriority) {
        this.routePriority = routePriority;
    }

    public BigDecimal getRouteCost() {
        return routeCost;
    }

    public void setRouteCost(BigDecimal routeCost) {
        this.routeCost = routeCost;
    }
}
