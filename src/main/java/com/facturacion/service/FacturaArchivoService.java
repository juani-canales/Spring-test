package com.facturacion.service;

import com.facturacion.DTO.FacturaReporteDTO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class FacturaArchivoService {

    public void generarPdf(List<FacturaReporteDTO> facturas, String rutaArchivo)
            throws IOException, DocumentException {

        Document documento = new Document();

        PdfWriter.getInstance(
                documento,
                new FileOutputStream(rutaArchivo)
        );

        documento.open();

        documento.add(
                new Paragraph("Reporte de Facturas")
        );

        documento.add(
                new Paragraph(" ")
        );

        PdfPTable tabla = new PdfPTable(7);

        tabla.addCell(new PdfPCell(new Phrase("Número")));
        tabla.addCell(new PdfPCell(new Phrase("Fecha")));
        tabla.addCell(new PdfPCell(new Phrase("Cliente")));
        tabla.addCell(new PdfPCell(new Phrase("Condición IVA")));
        tabla.addCell(new PdfPCell(new Phrase("Punto de Venta")));
        tabla.addCell(new PdfPCell(new Phrase("Importe Total")));
        tabla.addCell(new PdfPCell(new Phrase("Cantidad Items")));

        for (FacturaReporteDTO factura : facturas) {

            tabla.addCell(
                    String.valueOf(factura.getNumeroFactura())
            );

            tabla.addCell(
                    String.valueOf(factura.getFechaEmision())
            );

            tabla.addCell(
                    factura.getClienteDenominacion()
            );

            tabla.addCell(
                    factura.getCondicionIva()
            );

            tabla.addCell(
                    factura.getPuntoVentaDescripcion()
            );

            tabla.addCell(
                    String.valueOf(factura.getImporteTotal())
            );

            tabla.addCell(
                    String.valueOf(factura.getCantidadItems())
            );
        }

        documento.add(tabla);

        documento.close();
    }


    public void generarExcel(List<FacturaReporteDTO> facturas, String rutaArchivo)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet hoja = workbook.createSheet("Facturas");

        Row encabezado = hoja.createRow(0);

        encabezado.createCell(0).setCellValue("Número");
        encabezado.createCell(1).setCellValue("Fecha");
        encabezado.createCell(2).setCellValue("Cliente");
        encabezado.createCell(3).setCellValue("Condición IVA");
        encabezado.createCell(4).setCellValue("Punto de Venta");
        encabezado.createCell(5).setCellValue("Importe Total");
        encabezado.createCell(6).setCellValue("Cantidad Items");

        int fila = 1;

        for (FacturaReporteDTO factura : facturas) {

            Row row = hoja.createRow(fila++);

            row.createCell(0).setCellValue(
                    factura.getNumeroFactura()
            );

            row.createCell(1).setCellValue(
                    String.valueOf(factura.getFechaEmision())
            );

            row.createCell(2).setCellValue(
                    factura.getClienteDenominacion()
            );

            row.createCell(3).setCellValue(
                    factura.getCondicionIva()
            );

            row.createCell(4).setCellValue(
                    factura.getPuntoVentaDescripcion()
            );

            row.createCell(5).setCellValue(
                    factura.getImporteTotal()
            );

            row.createCell(6).setCellValue(
                    factura.getCantidadItems()
            );
        }

        for (int i = 0; i < 7; i++) {
            hoja.autoSizeColumn(i);
        }

        FileOutputStream archivo = new FileOutputStream(rutaArchivo);

        workbook.write(archivo);

        archivo.close();
        workbook.close();
    }
}