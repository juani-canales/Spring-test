package com.facturacion.DTO;

/** Datos livianos de un cliente para mostrar en el popover de búsqueda. */
public record ClienteResumenDTO(
        Long id,
        String denominacion,
        String cuitCuil,
        String email,
        String telefono,
        String nombreCalle,
        String numeroCalle
) {
}
