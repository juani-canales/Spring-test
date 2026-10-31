package com.facturacion.DTO;

import java.util.List;

/**
 * Respuesta paginada genérica. <T> es el tipo de cada elemento de la página
 * (FacturaReporteDTO, ClienteResumenDTO, etc.).
 * OJO: "pagina" es 0-based (la primera página es la 0).
 */
public record PaginaDTO<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {
    public static <T> PaginaDTO<T> de(List<T> contenido, int pagina, int tamanio, long totalElementos) {
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanio);
        return new PaginaDTO<>(contenido, pagina, tamanio, totalElementos, totalPaginas);
    }
}
