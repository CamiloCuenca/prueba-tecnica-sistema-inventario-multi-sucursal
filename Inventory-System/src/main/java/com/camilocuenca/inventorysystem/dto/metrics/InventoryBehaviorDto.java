package com.camilocuenca.inventorysystem.dto.metrics;

import java.util.List;

public class InventoryBehaviorDto {
    private List<InventoryRotationDto> rotations;
    private List<InventoryRotationDto> topHighDemand;
    private List<InventoryRotationDto> topLowDemand;

    public InventoryBehaviorDto() {
    }

    public List<InventoryRotationDto> getRotations() {
        return rotations;
    }

    public void setRotations(List<InventoryRotationDto> rotations) {
        this.rotations = rotations;
    }

    public List<InventoryRotationDto> getTopHighDemand() {
        return topHighDemand;
    }

    public void setTopHighDemand(List<InventoryRotationDto> topHighDemand) {
        this.topHighDemand = topHighDemand;
    }

    public List<InventoryRotationDto> getTopLowDemand() {
        return topLowDemand;
    }

    public void setTopLowDemand(List<InventoryRotationDto> topLowDemand) {
        this.topLowDemand = topLowDemand;
    }
}

