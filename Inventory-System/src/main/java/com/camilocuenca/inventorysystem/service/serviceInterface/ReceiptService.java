package com.camilocuenca.inventorysystem.service.serviceInterface;

import java.util.UUID;

/**
 * Interfaz para servicios que generan o entregan recibos/comprobantes (PDF) de ventas.
 */
public interface ReceiptService {
    /**
     * Genera o recupera el contenido PDF del comprobante de la venta indicada.
     * @param saleId id de la venta
     * @return contenido del PDF en bytes
     */
    byte[] getSaleReceiptPdf(UUID saleId);
}

