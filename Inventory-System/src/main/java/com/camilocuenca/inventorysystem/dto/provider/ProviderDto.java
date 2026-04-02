package com.camilocuenca.inventorysystem.dto.provider;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderDto {
    private UUID id;

    @NotBlank
    private String name;

    private String contactInfo;
}

