package com.facturacion.DTO;

public record FacturaDetalleRequestDTO(
        Long articuloId,
        double cantidad,
        String descripcion,
        double porcentajeBonificacion,
        double precioUnitario,
        double subtotal
) {
}