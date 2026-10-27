package com.facturacion.DTO;

import com.facturacion.entities.Cliente;
import com.facturacion.entities.ListaPrecioArticulo;

public record FacturaDetalleRequestDTO(
        Long articuloId,
        double cantidad,
        String descripcion,
        double porcentajeBonificacion
) {
}