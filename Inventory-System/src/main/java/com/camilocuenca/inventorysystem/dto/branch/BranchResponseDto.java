package com.camilocuenca.inventorysystem.dto.branch;

import java.time.Instant;
import java.util.UUID;

public record BranchResponseDto(
        UUID id,
        String name,
        String address,
        Double latitude,
        Double longitude,
        Instant createdAt
) {}

