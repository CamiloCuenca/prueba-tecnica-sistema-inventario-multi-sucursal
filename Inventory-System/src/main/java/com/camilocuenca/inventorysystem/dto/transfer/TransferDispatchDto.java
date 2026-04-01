package com.camilocuenca.inventorysystem.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class TransferDispatchDto {

    @NotBlank(message = "carrier es requerido")
    private String carrier;

    @NotNull(message = "estimatedArrival es requerido")
    private Instant estimatedArrival;

    // Logistics optional
    private String routeId;
    private String routePriority; // LOW, MEDIUM, HIGH, URGENT
    private Integer estimatedTransitMinutes;
    private Double routeCost;

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

    public Integer getEstimatedTransitMinutes() {
        return estimatedTransitMinutes;
    }

    public void setEstimatedTransitMinutes(Integer estimatedTransitMinutes) {
        this.estimatedTransitMinutes = estimatedTransitMinutes;
    }

    public Double getRouteCost() {
        return routeCost;
    }

    public void setRouteCost(Double routeCost) {
        this.routeCost = routeCost;
    }
}
