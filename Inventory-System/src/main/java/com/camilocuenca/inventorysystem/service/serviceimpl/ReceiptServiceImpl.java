package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.service.serviceInterface.ReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReceiptServiceImpl implements ReceiptService {

    private final PdfGenerator pdfGenerator;

    @Autowired
    public ReceiptServiceImpl(PdfGenerator pdfGenerator) {
        this.pdfGenerator = pdfGenerator;
    }

    @Override
    public byte[] getSaleReceiptPdf(UUID saleId) {
        return pdfGenerator.generateSaleReceipt(saleId);
    }
}

