package com.facturacion.DTO;

import java.util.List;

public record FacturaRequestDTO(
        Long numero,
        Long clienteId,
        Long condicionIvaId,
        Long puntoVentaId,
        Long tipoMonedaId,
        Long listaPrecioId,          // Solo se usa al CREAR (para validar los artículos); no se guarda
        Double importeCobrado,
        String observaciones,
        String estado,
        List<FacturaDetalleRequestDTO> detalles
) {
}
