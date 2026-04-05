package com.camilocuenca.inventorysystem.dto.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BranchUpdateDto(
        @NotNull UUID id,
        @NotNull @NotBlank String name,
        String address,
        Double latitude,
        Double longitude
) {}

