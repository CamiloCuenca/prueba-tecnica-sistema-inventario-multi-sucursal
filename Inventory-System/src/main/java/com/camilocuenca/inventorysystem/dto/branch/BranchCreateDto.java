package com.camilocuenca.inventorysystem.dto.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BranchCreateDto(
        @NotNull @NotBlank String name,
        String address,
        Double latitude,
        Double longitude
) {}

