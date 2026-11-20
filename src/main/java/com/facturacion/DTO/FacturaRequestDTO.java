package com.facturacion.DTO;

import java.util.List;

public record FacturaRequestDTO(
        Long clienteId,
        Long condicionIvaId,
        Long puntoVentaId,
        Long tipoMonedaId,
        Long listaPrecioId,          // Solo se usa al CREAR (para validar los artículos); no se guarda
        Double importeCobrado,
        String observaciones,
        String estado,
        Boolean informar,           // true = pedir el CAE a ARCA; null/false = solo guardar
        List<FacturaDetalleRequestDTO> detalles
) {
}
