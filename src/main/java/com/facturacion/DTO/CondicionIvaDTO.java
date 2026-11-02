package com.facturacion.DTO;

/** Datos mínimos de una condición de IVA para llenar el <select> del formulario. */
public record CondicionIvaDTO(
        Long id,
        String denominacion
) {
}