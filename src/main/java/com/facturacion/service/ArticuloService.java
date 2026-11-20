package com.facturacion.service;

import com.facturacion.DTO.ArticuloPrecioDTO;
import com.facturacion.DTO.PaginaDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticuloService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public PaginaDTO<ArticuloPrecioDTO> buscar(Long listaPrecioId, String codigo, String denominacion,
                                               String marca, String rubro, Double precioMinimo,
                                               int pagina, int tamanio) {
        // Protegemos al backend de valores raros (página negativa, tamanio = 100000...)
        int pag = Math.max(pagina, 0);
        int tam = Math.min(Math.max(tamanio, 1), 100);

        // La parte común (FROM + WHERE) se arma UNA vez y la usan las dos consultas.
        // Marca y rubro son opcionales: LEFT JOIN para no perder artículos que no los tengan.
        StringBuilder desde = new StringBuilder(
                " FROM ListaPrecioArticulo lpa " +
                        "JOIN lpa.articulo a " +
                        "LEFT JOIN a.marca m " +
                        "LEFT JOIN a.rubro r " +
                        "WHERE lpa.fechaBaja IS NULL AND lpa.listaPrecio.id = :listaPrecioId ");
        Map<String, Object> params = new HashMap<>();
        params.put("listaPrecioId", listaPrecioId);

        if (hayTexto(codigo)) {
            desde.append("AND LOWER(a.codigo) LIKE :codigo ");
            params.put("codigo", "%" + codigo.trim().toLowerCase() + "%");
        }
        if (hayTexto(denominacion)) {
            desde.append("AND LOWER(a.denominacion) LIKE :denominacion ");
            params.put("denominacion", "%" + denominacion.trim().toLowerCase() + "%");
        }
        if (hayTexto(marca)) {
            desde.append("AND LOWER(m.denominacion) LIKE :marca ");
            params.put("marca", "%" + marca.trim().toLowerCase() + "%");
        }
        if (hayTexto(rubro)) {
            desde.append("AND LOWER(r.denominacion) LIKE :rubro ");
            params.put("rubro", "%" + rubro.trim().toLowerCase() + "%");
        }
        if (precioMinimo != null) {
            // "igual o mayor": filtrar por 5000 trae los artículos de 5000 y más
            desde.append("AND lpa.precioVenta >= :precioMinimo ");
            params.put("precioMinimo", precioMinimo);
        }

        // 1) ¿Cuántos artículos hay EN TOTAL con estos filtros?
        TypedQuery<Long> consultaTotal = em.createQuery("SELECT COUNT(lpa)" + desde, Long.class);
        params.forEach(consultaTotal::setParameter);
        long total = consultaTotal.getSingleResult();

        // 2) Solo los artículos de ESTA página (6 argumentos, en el orden del record)
        TypedQuery<ArticuloPrecioDTO> consultaPagina = em.createQuery(
                "SELECT new com.facturacion.DTO.ArticuloPrecioDTO(" +
                        "lpa.id, a.codigo, a.denominacion, lpa.precioVenta, m.denominacion, r.denominacion)" +
                        desde + " ORDER BY a.denominacion ASC, lpa.id ASC",
                ArticuloPrecioDTO.class);
        params.forEach(consultaPagina::setParameter);
        consultaPagina.setFirstResult(pag * tam);   // "saltear" las páginas anteriores
        consultaPagina.setMaxResults(tam);          // "traer solo tam registros"

        List<ArticuloPrecioDTO> contenido = consultaPagina.getResultList();
        return PaginaDTO.de(contenido, pag, tam, total);
    }

    private boolean hayTexto(String s) {
        return s != null && !s.trim().isEmpty();
    }
}