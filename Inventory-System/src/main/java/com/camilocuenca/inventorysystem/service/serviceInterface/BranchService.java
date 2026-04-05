package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.branch.BranchCreateDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchResponseDto;
import com.camilocuenca.inventorysystem.dto.branch.BranchUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BranchService {

    /**
     * Crea una nueva sucursal.
     */
    BranchResponseDto createBranch(BranchCreateDto dto) throws Exception;

    /**
     * Lista sucursales paginadas.
     */
    Page<BranchResponseDto> listBranches(Pageable pageable);

    /**
     * Obtiene una sucursal por su id.
     */
    BranchResponseDto getBranch(UUID id) throws Exception;

    /**
     * Actualiza una sucursal existente.
     */
    BranchResponseDto updateBranch(BranchUpdateDto dto) throws Exception;

    /**
     * Elimina una sucursal por id.
     */
    void deleteBranch(UUID id) throws Exception;
}

