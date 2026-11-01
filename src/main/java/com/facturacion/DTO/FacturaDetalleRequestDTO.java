package com.facturacion.DTO;

public record FacturaDetalleRequestDTO(
        Long articuloId,
        Double cantidad,
        String descripcion,
        Double porcentajeBonificacion,
        Double precioUnitario,
        Double subtotal
) {
}