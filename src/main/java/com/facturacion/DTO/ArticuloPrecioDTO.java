package com.facturacion.DTO;

/**
 * Artículo con su precio de venta, para armar los renglones de la factura.
 * listaPrecioArticuloId es el ID de ListaPrecioArticulo (NO el del Articulo):
 * es el que el backend espera en FacturaDetalleRequestDTO.articuloId.
 */
public record ArticuloPrecioDTO(
        Long listaPrecioArticuloId,
        String codigo,
        String denominacion,
        double precioVenta
) {
}