package com.camilocuenca.inventorysystem.service.serviceimpl;

import com.camilocuenca.inventorysystem.model.Sale;
import com.camilocuenca.inventorysystem.model.SaleDetail;
import com.camilocuenca.inventorysystem.repository.SaleDetailRepository;
import com.camilocuenca.inventorysystem.repository.SaleRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PdfGenerator {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;

    @Autowired
    public PdfGenerator(SaleRepository saleRepository, SaleDetailRepository saleDetailRepository) {
        this.saleRepository = saleRepository;
        this.saleDetailRepository = saleDetailRepository;
    }

    /**
     * Genera un comprobante PDF para la venta indicada y lo devuelve como un array de bytes.
     * @param saleId id de la venta
     * @return contenido del PDF
     */
    public byte[] generateSaleReceipt(UUID saleId) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada"));

        List<SaleDetail> details = saleDetailRepository.findBySaleId(saleId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Header
            Paragraph title = new Paragraph("Comprobante de Venta", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            // Meta info: sale id, fecha, sucursal, usuario
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
            String fecha = sale.getCreatedAt() != null ? dtf.format(sale.getCreatedAt()) : "-";

            PdfPTable meta = new PdfPTable(2);
            meta.setWidthPercentage(100);
            meta.setSpacingBefore(8);

            PdfPCell c1 = new PdfPCell(new Phrase("Venta ID:", smallFont));
            c1.setBorder(Rectangle.NO_BORDER);
            PdfPCell c2 = new PdfPCell(new Phrase(sale.getId().toString(), smallFont));
            c2.setBorder(Rectangle.NO_BORDER);
            meta.addCell(c1);
            meta.addCell(c2);

            PdfPCell c3 = new PdfPCell(new Phrase("Fecha:", smallFont));
            c3.setBorder(Rectangle.NO_BORDER);
            PdfPCell c4 = new PdfPCell(new Phrase(fecha, smallFont));
            c4.setBorder(Rectangle.NO_BORDER);
            meta.addCell(c3);
            meta.addCell(c4);

            String branchName = sale.getBranch() != null ? sale.getBranch().getName() : "-";
            PdfPCell c5 = new PdfPCell(new Phrase("Sucursal:", smallFont));
            c5.setBorder(Rectangle.NO_BORDER);
            PdfPCell c6 = new PdfPCell(new Phrase(branchName, smallFont));
            c6.setBorder(Rectangle.NO_BORDER);
            meta.addCell(c5);
            meta.addCell(c6);

            String userName = (sale.getUser() != null) ? sale.getUser().getName() : "-";
            PdfPCell c7 = new PdfPCell(new Phrase("Responsable:", smallFont));
            c7.setBorder(Rectangle.NO_BORDER);
            PdfPCell c8 = new PdfPCell(new Phrase(userName, smallFont));
            c8.setBorder(Rectangle.NO_BORDER);
            meta.addCell(c7);
            meta.addCell(c8);

            document.add(meta);
            document.add(new Paragraph(" "));

            // Items table
            PdfPTable table = new PdfPTable(new float[] {4, 1, 2, 2});
            table.setWidthPercentage(100);
            addTableHeader(table, normalFont);

            DecimalFormat df = new DecimalFormat("#,##0.00");
            BigDecimal calcTotal = BigDecimal.ZERO;

            for (SaleDetail d : details) {
                String prodName = "-";
                if (d.getProduct() != null) {
                    prodName = d.getProduct().getName() != null ? d.getProduct().getName() : d.getProduct().getId().toString();
                }
                String qty = d.getQuantity() != null ? df.format(d.getQuantity()) : "0";
                String price = d.getPrice() != null ? df.format(d.getPrice()) : "0.00";
                BigDecimal lineTotal = (d.getPrice() != null ? d.getPrice() : BigDecimal.ZERO).multiply(d.getQuantity() != null ? d.getQuantity() : BigDecimal.ZERO);
                calcTotal = calcTotal.add(lineTotal);
                String line = df.format(lineTotal);

                addRow(table, prodName, qty, price, line, normalFont);
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // Totales
            PdfPTable totals = new PdfPTable(2);
            totals.setWidthPercentage(40);
            totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totals.setSpacingBefore(6);

            PdfPCell t1 = new PdfPCell(new Phrase("Subtotal:", smallFont));
            t1.setBorder(Rectangle.NO_BORDER);
            PdfPCell t2 = new PdfPCell(new Phrase(df.format(calcTotal), smallFont));
            t2.setBorder(Rectangle.NO_BORDER);
            totals.addCell(t1);
            totals.addCell(t2);

            BigDecimal discount = sale.getTotal() != null ? calcTotal.subtract(sale.getTotal()) : BigDecimal.ZERO;
            PdfPCell t3 = new PdfPCell(new Phrase("Descuento:", smallFont));
            t3.setBorder(Rectangle.NO_BORDER);
            PdfPCell t4 = new PdfPCell(new Phrase(df.format(discount), smallFont));
            t4.setBorder(Rectangle.NO_BORDER);
            totals.addCell(t3);
            totals.addCell(t4);

            PdfPCell t5 = new PdfPCell(new Phrase("Total:", titleFont()));
            t5.setBorder(Rectangle.NO_BORDER);
            PdfPCell t6 = new PdfPCell(new Phrase(df.format(sale.getTotal() != null ? sale.getTotal() : calcTotal), titleFont()));
            t6.setBorder(Rectangle.NO_BORDER);
            totals.addCell(t5);
            totals.addCell(t6);

            document.add(totals);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generando PDF: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase("Producto", font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Cant.", font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Precio", font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Total", font));
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }

    private void addRow(PdfPTable table, String c1, String c2, String c3, String c4, Font font) {
        table.addCell(new PdfPCell(new Phrase(c1, font)));
        PdfPCell q = new PdfPCell(new Phrase(c2, font));
        q.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(q);
        PdfPCell p = new PdfPCell(new Phrase(c3, font));
        p.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(p);
        PdfPCell t = new PdfPCell(new Phrase(c4, font));
        t.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(t);
    }

    private Font titleFont() {
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    }
}
