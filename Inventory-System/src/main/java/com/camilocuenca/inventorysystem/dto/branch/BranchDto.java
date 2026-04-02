package com.camilocuenca.inventorysystem.dto.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO simple para exponer el identificador y nombre de una sucursal.
 */
public class BranchDto {

    @NotNull
    private UUID id;

    @NotBlank
    private String name;

    public BranchDto() {
    }

    public BranchDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

