package com.facturacion.service;

import com.facturacion.entities.Articulo;
import com.facturacion.entities.Cliente;
import com.facturacion.entities.FacturaVenta;
import com.facturacion.entities.FacturaVentaDetalle;
import com.facturacion.entities.PuntoVenta;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Genera el comprobante PDF (A4) de una factura EMITIDA.
 * Es una vista de SALIDA: solo lee la base de datos, no guarda nada y no llama a ARCA.
 */
@Service
public class FacturaPdfService {

    /** El PDF ya armado (en memoria) y el nombre sugerido para el archivo. */
    public record ComprobantePdf(String nombreArchivo, byte[] contenido) {}

    @PersistenceContext
    private EntityManager em;

    @Value("${afip.cuit}")
    private long cuitEmisor;

    @Value("${afip.tipo-comprobante}")
    private int cbteTipo;

    // Con valor por defecto (lo que va después de los dos puntos): si falta la propiedad, no falla el arranque.
    @Value("${emisor.razon-social:Configurar emisor.razon-social}")
    private String razonSocial;

    @Value("${emisor.domicilio:}")
    private String domicilioEmisor;

    @Value("${emisor.condicion-iva:}")
    private String condicionIvaEmisor;

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font FUENTE_LETRA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 32);
    private static final Font FUENTE_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
    private static final Font FUENTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9);
    private static final Font FUENTE_CHICA = FontFactory.getFont(FontFactory.HELVETICA, 7);
    private static final Font FUENTE_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);

    // @Transactional es necesario: factura.getCliente(), getPuntoVenta(), getDetalles()... son LAZY
    // y solo se pueden leer mientras la transacción (la "sesión" de Hibernate) sigue abierta.
    @Transactional(readOnly = true)
    public ComprobantePdf generar(Long id) {
        FacturaVenta factura = em.find(FacturaVenta.class, id);
        if (factura == null) {
            throw new EntityNotFoundException("Factura no encontrada con ID: " + id);
        }
        if (!"EMITIDA".equals(factura.getEstado()) || !hayTexto(factura.getCae())) {
            throw new IllegalArgumentException(
                    "Solo se puede generar el PDF de una factura EMITIDA (con CAE). Estado actual: "
                            + factura.getEstado() + ".");
        }

        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();   // el PDF se arma en memoria
            Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(crearCabecera(factura));
            documento.add(crearBloqueCliente(factura));
            documento.add(crearTablaDetalle(factura));
            documento.add(crearTotales(factura));
            documento.add(crearPieFiscal(factura));

            documento.close();
            return new ComprobantePdf(nombreArchivo(factura), salida.toByteArray());
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }

    // ------ Bloques del comprobante

    /** Encabezado: [emisor] [letra] [datos del comprobante]. */
    private PdfPTable crearCabecera(FacturaVenta f) {
        PuntoVenta pv = f.getPuntoVenta();

        PdfPCell emisor = new PdfPCell();
        emisor.setPadding(8);
        emisor.addElement(new Paragraph(razonSocial, FUENTE_TITULO));
        emisor.addElement(linea("Domicilio comercial", domicilioDelEmisor(pv)));
        emisor.addElement(linea("Condición frente al IVA", condicionIvaDelEmisor()));

        Paragraph letra = new Paragraph(letraComprobante(), FUENTE_LETRA);
        letra.setAlignment(Element.ALIGN_CENTER);
        Paragraph codigo = new Paragraph(String.format("COD. %02d", cbteTipo), FUENTE_CHICA);
        codigo.setAlignment(Element.ALIGN_CENTER);
        PdfPCell centro = new PdfPCell();
        centro.setPadding(4);
        centro.addElement(letra);
        centro.addElement(codigo);

        PdfPCell datos = new PdfPCell();
        datos.setPadding(8);
        datos.addElement(new Paragraph("FACTURA", FUENTE_TITULO));
        datos.addElement(linea("Punto de venta", String.format("%04d", pv.getNumero())));
        datos.addElement(linea("Comp. Nro", String.format("%08d", f.getNumero())));
        datos.addElement(linea("Fecha de emisión", formatearFecha(f.getFechaEmision())));
        datos.addElement(linea("CUIT", formatearCuit(String.valueOf(cuitEmisor))));

        PdfPTable tabla = new PdfPTable(new float[]{45f, 10f, 45f});
        tabla.setWidthPercentage(100);
        tabla.addCell(emisor);
        tabla.addCell(centro);
        tabla.addCell(datos);
        return tabla;
    }

    /** Datos del cliente (receptor). */
    private PdfPTable crearBloqueCliente(FacturaVenta f) {
        Cliente c = f.getCliente();   // puede ser null en facturas viejas
        String nombre = (c != null) ? c.getDenominacion() : "Consumidor Final";

        PdfPCell izquierda = new PdfPCell();
        izquierda.setPadding(8);
        izquierda.addElement(linea("Razón social / Nombre", nombre));
        izquierda.addElement(linea("Domicilio", describirDomicilio(c)));

        PdfPCell derecha = new PdfPCell();
        derecha.setPadding(8);
        derecha.addElement(linea("Documento", describirDocumento(c)));
        derecha.addElement(linea("Condición frente al IVA", f.getCondicionIva().getDenominacion()));

        PdfPTable tabla = new PdfPTable(new float[]{55f, 45f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(8f);
        tabla.addCell(izquierda);
        tabla.addCell(derecha);
        return tabla;
    }

    /** Detalle: una fila por renglón. Si hay muchos, continúa en otra página repitiendo el encabezado. */
    private PdfPTable crearTablaDetalle(FacturaVenta f) {
        PdfPTable tabla = new PdfPTable(new float[]{12f, 32f, 9f, 17f, 10f, 20f});
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(10f);
        tabla.setHeaderRows(1);   // la fila 0 se repite en cada página

        for (String titulo : new String[]{"Código", "Descripción", "Cant.", "Precio unit. (s/IVA)", "% Bonif.", "Subtotal (c/IVA)"}) {
            tabla.addCell(celdaEncabezado(titulo));
        }

        for (FacturaVentaDetalle d : f.getDetalles()) {
            Articulo articulo = d.getListaPrecioArticulo().getArticulo();
            String descripcion = hayTexto(d.getDescripcion()) ? d.getDescripcion() : articulo.getDenominacion();

            tabla.addCell(celdaDetalle(articulo.getCodigo(), Element.ALIGN_LEFT));
            tabla.addCell(celdaDetalle(descripcion, Element.ALIGN_LEFT));
            tabla.addCell(celdaDetalle(formatearNumero(d.getCantidad()), Element.ALIGN_RIGHT));
            tabla.addCell(celdaDetalle(formatearMoneda(d.getPrecioUnitario()), Element.ALIGN_RIGHT));
            tabla.addCell(celdaDetalle(formatearNumero(d.getPorcentajeBonificacion()), Element.ALIGN_RIGHT));
            tabla.addCell(celdaDetalle(formatearMoneda(d.getImporteSubtotal()), Element.ALIGN_RIGHT));
        }
        return tabla;
    }

    /** Totales. En Factura C no se discrimina el IVA: solo se muestra el total. */
    private PdfPTable crearTotales(FacturaVenta f) {
        double neto = 0;
        double iva = 0;
        for (FacturaVentaDetalle d : f.getDetalles()) {
            neto += d.getImporteNeto();
            iva += d.getImporteIva();
        }

        PdfPTable tabla = new PdfPTable(new float[]{55f, 45f});
        tabla.setWidthPercentage(45);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingBefore(10f);

        if (!"C".equals(letraComprobante())) {
            agregarFilaTotal(tabla, "Importe neto gravado", formatearMoneda(neto), FUENTE_NORMAL);
            agregarFilaTotal(tabla, "IVA 21%", formatearMoneda(iva), FUENTE_NORMAL);
        }
        agregarFilaTotal(tabla, "IMPORTE TOTAL", formatearMoneda(f.getImporteTotal()), FUENTE_TOTAL);
        return tabla;
    }

    /** Pie fiscal: CAE y vencimiento. */
    private PdfPTable crearPieFiscal(FacturaVenta f) {
        PdfPCell celda = new PdfPCell();
        celda.setPadding(8);
        celda.addElement(linea("CAE N°", f.getCae()));
        celda.addElement(linea("Fecha de vencimiento del CAE", formatearFecha(f.getCaeFechaVencimiento())));

        PdfPTable tabla = new PdfPTable(1);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(16f);
        tabla.addCell(celda);
        return tabla;
    }

    // ------ Pequeñas ayudas de armado

    /** "Etiqueta: valor" con la etiqueta en negrita. */
    private Paragraph linea(String etiqueta, String valor) {
        Paragraph p = new Paragraph();
        p.setLeading(12f);
        p.add(new Chunk(etiqueta + ": ", FUENTE_NEGRITA));
        p.add(new Chunk(hayTexto(valor) ? valor : "-", FUENTE_NORMAL));
        return p;
    }

    private PdfPCell celdaEncabezado(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, FUENTE_NEGRITA));
        c.setBackgroundColor(new Color(230, 230, 230));
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setPadding(5);
        return c;
    }

    private PdfPCell celdaDetalle(String texto, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, FUENTE_NORMAL));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(4);
        c.setBorderColor(new Color(200, 200, 200));
        return c;
    }

    private void agregarFilaTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        PdfPCell izquierda = new PdfPCell(new Phrase(etiqueta, fuente));
        izquierda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        izquierda.setPadding(4);
        PdfPCell derecha = new PdfPCell(new Phrase(valor, fuente));
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        derecha.setPadding(4);
        tabla.addCell(izquierda);
        tabla.addCell(derecha);
    }

    // ------ Textos y formatos

    /** El tipo de comprobante sale de la configuración (la factura no lo guarda). */
    private String letraComprobante() {
        return switch (cbteTipo) {
            case 1 -> "A";
            case 6 -> "B";
            case 11 -> "C";
            default -> "X";
        };
    }

    private String domicilioDelEmisor(PuntoVenta pv) {
        return hayTexto(pv.getDomicilioComercial()) ? pv.getDomicilioComercial() : domicilioEmisor;
    }

    private String condicionIvaDelEmisor() {
        if (hayTexto(condicionIvaEmisor)) return condicionIvaEmisor;
        return "C".equals(letraComprobante()) ? "Responsable Monotributo" : "IVA Responsable Inscripto";
    }

    /** Mismo criterio que AfipFacturacionService: CUIT/CUIL en blanco o en 0 = sin identificar. */
    private String describirDocumento(Cliente c) {
        String sinIdentificar = "Sin identificar (Consumidor Final)";
        if (c == null || c.getCuitCuil() == null) return sinIdentificar;
        String digitos = c.getCuitCuil().replaceAll("\\D", "");
        if (digitos.isEmpty() || digitos.matches("0+")) return sinIdentificar;
        return "CUIT/CUIL " + c.getCuitCuil();
    }

    private String describirDomicilio(Cliente c) {
        if (c == null || c.getDomicilio() == null) return "-";
        String calle = c.getDomicilio().getNombreCalle() != null ? c.getDomicilio().getNombreCalle() : "";
        String numero = c.getDomicilio().getNumeroCalle() != null ? c.getDomicilio().getNumeroCalle() : "";
        String domicilio = (calle + " " + numero).trim();
        return domicilio.replace("-", "").trim().isEmpty() ? "-" : domicilio;   // "- -" = sin datos
    }

    private String nombreArchivo(FacturaVenta f) {
        return String.format("factura-%04d-%08d.pdf", f.getPuntoVenta().getNumero(), f.getNumero());
    }

    private static boolean hayTexto(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String formatearFecha(Date fecha) {
        return fecha == null ? "-" : new SimpleDateFormat("dd/MM/yyyy").format(fecha);
    }

    // Formato argentino: punto para los miles y coma para los decimales (1.234,56).
    // DecimalFormat no es thread-safe: se crea uno nuevo en cada llamada.
    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.of("es", "AR"));

    private static String formatearMoneda(double valor) {
        return "$ " + new DecimalFormat("#,##0.00", SIMBOLOS).format(valor);
    }

    private static String formatearNumero(double valor) {
        return new DecimalFormat("#,##0.##", SIMBOLOS).format(valor);   // 2 -> "2", 1.5 -> "1,5"
    }

    private static String formatearCuit(String digitos) {
        return digitos.length() == 11
                ? digitos.substring(0, 2) + "-" + digitos.substring(2, 10) + "-" + digitos.substring(10)
                : digitos;
    }
}