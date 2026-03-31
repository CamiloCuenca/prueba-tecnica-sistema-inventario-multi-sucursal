package com.camilocuenca.inventorysystem.dto.jwt;

public record MessageDTO<T>(
        boolean error,
        T respuesta
) {
}