package com.camilocuenca.inventorysystem.util;

import com.camilocuenca.inventorysystem.Enums.TransferStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Convierte entre el valor String almacenado en la BD y el enum TransferStatus.
 * Además normaliza variantes legacy (EN_TRANSITO vs EN_TRANSIT, etc.) para
 * permitir una migración suave sin romper lectura de datos existentes.
 */
@Converter(autoApply = true)
public class TransferStatusAttributeConverter implements AttributeConverter<TransferStatus, String> {

    @Override
    public String convertToDatabaseColumn(TransferStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public TransferStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        String v = dbData.trim().toUpperCase();
        switch (v) {
            case "PENDING":
                return TransferStatus.PENDING;
            case "PREPARING":
            case "PREPARANDO":
                return TransferStatus.PREPARING;
            case "SHIPPED":
                return TransferStatus.SHIPPED;
            case "PARTIALLY_SHIPPED":
            case "PARTIALLY-SHIPPED":
                return TransferStatus.PARTIALLY_SHIPPED;
            case "EN_TRANSITO":
            case "EN_TRANSIT":
            case "IN_TRANSIT":
                return TransferStatus.IN_TRANSIT;
            case "PARTIALLY_RECEIVED":
            case "PARCIALMENTE_RECIBIDO":
                return TransferStatus.PARTIALLY_RECEIVED;
            case "RECEIVED":
            case "RECIBIDO":
                return TransferStatus.RECEIVED;
            case "CANCELLED":
            case "CANCELADO":
                return TransferStatus.CANCELLED;
            default:
                // Fallback conservador: tratar como PENDING y loggear si es necesario
                return TransferStatus.PENDING;
        }
    }
}
