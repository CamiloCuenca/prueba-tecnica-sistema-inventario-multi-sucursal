package com.camilocuenca.inventorysystem.dto.transfer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TransferResponseDto {

    private UUID id;
    private UUID originBranchId;
    private UUID destinationBranchId;
    private String status;
    private Instant createdAt;
    private Instant shippedAt;
    private Instant dispatchedAt;
    private Instant receivedAt;
    private String carrier;
    private Instant estimatedArrival;
    // Logistics
    private String routeId;
    private String routePriority;
    private Integer estimatedTransitMinutes;
    private Integer actualTransitMinutes;
    private Double routeCost;
    private List<TransferDetailResponseDto> items;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOriginBranchId() {
        return originBranchId;
    }

    public void setOriginBranchId(UUID originBranchId) {
        this.originBranchId = originBranchId;
    }

    public UUID getDestinationBranchId() {
        return destinationBranchId;
    }

    public void setDestinationBranchId(UUID destinationBranchId) {
        this.destinationBranchId = destinationBranchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Instant shippedAt) {
        this.shippedAt = shippedAt;
    }

    public Instant getDispatchedAt() {
        return dispatchedAt;
    }

    public void setDispatchedAt(Instant dispatchedAt) {
        this.dispatchedAt = dispatchedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

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

    public Integer getActualTransitMinutes() {
        return actualTransitMinutes;
    }

    public void setActualTransitMinutes(Integer actualTransitMinutes) {
        this.actualTransitMinutes = actualTransitMinutes;
    }

    public Double getRouteCost() {
        return routeCost;
    }

    public void setRouteCost(Double routeCost) {
        this.routeCost = routeCost;
    }

    public List<TransferDetailResponseDto> getItems() {
        return items;
    }

    public void setItems(List<TransferDetailResponseDto> items) {
        this.items = items;
    }
}
