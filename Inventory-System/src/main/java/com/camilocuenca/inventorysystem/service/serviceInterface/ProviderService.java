package com.camilocuenca.inventorysystem.service.serviceInterface;

import com.camilocuenca.inventorysystem.dto.product.ProductByProviderDto;
import com.camilocuenca.inventorysystem.dto.provider.ProviderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Contrato para operaciones sobre proveedores.
 */
public interface ProviderService {

    /**
     * Crea un nuevo proveedor en el sistema.
     * @param dto datos del proveedor
     * @return proveedor creado con id
     * @throws Exception en caso de validación o errores
     */
    ProviderDto createProvider(ProviderDto dto) throws Exception;

    /**
     * Actualiza un proveedor existente.
     */
    ProviderDto updateProvider(UUID id, ProviderDto dto) throws Exception;

    /**
     * Obtiene un proveedor por su id.
     */
    ProviderDto getProviderById(UUID id) throws Exception;

    /**
     * Elimina un proveedor por id.
     */
    void deleteProvider(UUID id) throws Exception;

    /**
     * Lista paginada de proveedores.
     */
    Page<ProviderDto> listProviders(Pageable pageable);

    /**
     * Lista paginada de productos asociados a un proveedor.
     */
    Page<ProductByProviderDto> listProductsByProvider(UUID providerId, Pageable pageable);

}

