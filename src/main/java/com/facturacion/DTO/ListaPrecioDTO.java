package com.facturacion.DTO;

/** Datos mínimos de una lista de precios para llenar el <select> del formulario. */
public record ListaPrecioDTO(
        Long id,
        String codigo,
        String denominacion
) {
}