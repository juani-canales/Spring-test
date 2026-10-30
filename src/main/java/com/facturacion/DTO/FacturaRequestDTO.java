package com.facturacion.DTO;

import java.util.List;

public record FacturaRequestDTO(
        Long numero,
        Long clienteId,
        Long condicionIvaId,
        Long puntoVentaId,
        Long tipoMonedaId,
        Double importeCobrado,
        String observaciones,
        List<FacturaDetalleRequestDTO> detalles
) {
}
