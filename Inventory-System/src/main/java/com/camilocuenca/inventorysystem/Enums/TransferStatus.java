package com.camilocuenca.inventorysystem.Enums;

/**
 * Enum que representa los estados posibles de una transferencia.
 * Usar enums garantiza integridad y evita variantes textuales en la BD.
 */
public enum TransferStatus {
    PENDING,
    PREPARING,
    SHIPPED,
    PARTIALLY_SHIPPED,
    IN_TRANSIT,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CANCELLED
}

