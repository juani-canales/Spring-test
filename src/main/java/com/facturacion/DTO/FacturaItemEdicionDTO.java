package com.facturacion.DTO;

/**
 * Un renglón de una factura ya guardada, para precargar la pantalla de edición.
 * listaPrecioArticuloId es el ID de ListaPrecioArticulo (el mismo que usa el alta).
 */
public record FacturaItemEdicionDTO(
        Long listaPrecioArticuloId,
        String codigo,
        String denominacion,
        double precioUnitario,
        double cantidad,
        double porcentajeBonificacion
) {
}