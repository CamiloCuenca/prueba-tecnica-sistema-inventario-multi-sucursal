package com.camilocuenca.inventorysystem.dto.metrics;

import java.math.BigDecimal;
import java.util.UUID;

public class BranchPerformanceDto {
    private UUID branchId;
    private String branchName;
    private BigDecimal totalRevenue;

    public BranchPerformanceDto() {}

    public UUID getBranchId() {
        return branchId;
    }

    public void setBranchId(UUID branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}

