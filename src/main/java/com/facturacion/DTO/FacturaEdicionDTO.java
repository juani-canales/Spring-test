package com.facturacion.DTO;

import java.util.Date;
import java.util.List;

/**
 * Factura completa para precargar la pantalla de edición (GET /api/facturas/{id}).
 * Incluye los IDs Y los textos: los campos deshabilitados solo se muestran, no se editan.
 * clienteId puede ser null (Consumidor Final).
 * La lista de precios NO se guarda en la factura: se DEDUCE de sus renglones (puede ser null si no tiene renglones).
 */
public record FacturaEdicionDTO(
        Long id,
        Long numero,
        Date fechaEmision,
        Long puntoVentaId,
        String puntoVentaDescripcion,
        Long clienteId,
        String clienteDenominacion,
        Long condicionIvaId,
        String condicionIvaDenominacion,
        Long listaPrecioId,
        String listaPrecioDescripcion,
        String estado,
        List<FacturaItemEdicionDTO> detalles
) {
}