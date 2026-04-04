package com.camilocuenca.inventorysystem.dto.user;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String name,
        String email,
        String role,
        UUID branchId,
        Instant createdAt
) {
}

