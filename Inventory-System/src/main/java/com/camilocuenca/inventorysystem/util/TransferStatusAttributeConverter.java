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
        return switch (v) {
            case "PENDING" -> TransferStatus.PENDING;
            case "PREPARING", "PREPARANDO" -> TransferStatus.PREPARING;
            case "SHIPPED" -> TransferStatus.SHIPPED;
            case "PARTIALLY_SHIPPED", "PARTIALLY-SHIPPED" -> TransferStatus.PARTIALLY_SHIPPED;
            case "EN_TRANSITO", "EN_TRANSIT", "IN_TRANSIT" -> TransferStatus.IN_TRANSIT;
            case "PARTIALLY_RECEIVED", "PARCIALMENTE_RECIBIDO" -> TransferStatus.PARTIALLY_RECEIVED;
            case "RECEIVED", "RECIBIDO" -> TransferStatus.RECEIVED;
            case "CANCELLED", "CANCELADO" -> TransferStatus.CANCELLED;
            default -> TransferStatus.PENDING;
        };
    }
}
