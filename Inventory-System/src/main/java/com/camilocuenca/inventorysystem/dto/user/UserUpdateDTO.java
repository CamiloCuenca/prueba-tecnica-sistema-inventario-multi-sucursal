package com.camilocuenca.inventorysystem.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserUpdateDTO(
        @NotNull UUID id,
        @NotBlank String name,
        @NotNull @Email String email,
        String role,
        UUID branchId
) {}

