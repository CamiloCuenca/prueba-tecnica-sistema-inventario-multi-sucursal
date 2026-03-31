package com.camilocuenca.inventorysystem.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRegisterDTO(
        @NotNull
        @NotBlank
        String name,
        @NotNull
        @Email
        String email,
        @NotNull
        @NotBlank
        String password,
        String role,
        UUID branchId
) {
}

