package com.facturacion.DTO;

/** Datos mínimos de un punto de venta para llenar el <select> del formulario. */
public record PuntoVentaDTO(
        Long id,
        int numero,
        String descripcion
) {
}