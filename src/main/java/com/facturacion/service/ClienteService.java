package com.facturacion.service;

import com.facturacion.DTO.ClienteResumenDTO;
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
public class ClienteService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public PaginaDTO<ClienteResumenDTO> buscar(String denominacion, String cuitCuil, String contacto,
                                               String domicilio, int pagina, int tamanio) {
        // Protegemos al backend de valores raros (página negativa, tamanio = 100000...)
        int pag = Math.max(pagina, 0);
        int tam = Math.min(Math.max(tamanio, 1), 100);

        // La parte común (FROM + WHERE) se arma UNA vez y la usan las dos consultas
        StringBuilder desde = new StringBuilder(
                " FROM Cliente c JOIN c.contacto ct JOIN c.domicilio d WHERE c.fechaBaja IS NULL ");
        Map<String, Object> params = new HashMap<>();

        if (hayTexto(denominacion)) {
            desde.append("AND LOWER(c.denominacion) LIKE :denominacion ");
            params.put("denominacion", "%" + denominacion.trim().toLowerCase() + "%");
        }
        if (hayTexto(cuitCuil)) {
            desde.append("AND c.cuitCuil LIKE :cuitCuil ");
            params.put("cuitCuil", "%" + cuitCuil.trim() + "%");
        }
        if (hayTexto(contacto)) {
            desde.append("AND (LOWER(ct.email) LIKE :contacto OR ct.telefono LIKE :contacto OR ct.celular LIKE :contacto) ");
            params.put("contacto", "%" + contacto.trim().toLowerCase() + "%");
        }
        if (hayTexto(domicilio)) {
            desde.append("AND LOWER(d.nombreCalle) LIKE :domicilio ");
            params.put("domicilio", "%" + domicilio.trim().toLowerCase() + "%");
        }

        // 1) ¿Cuántos clientes hay EN TOTAL con estos filtros?
        TypedQuery<Long> consultaTotal = em.createQuery("SELECT COUNT(c)" + desde, Long.class);
        params.forEach(consultaTotal::setParameter);
        long total = consultaTotal.getSingleResult();

        // 2) Solo los clientes de ESTA página
        TypedQuery<ClienteResumenDTO> consultaPagina = em.createQuery(
                "SELECT new com.facturacion.DTO.ClienteResumenDTO(" +
                        "c.id, c.denominacion, c.cuitCuil, ct.email, ct.telefono, d.nombreCalle, d.numeroCalle)" +
                        desde + " ORDER BY c.denominacion ASC",
                ClienteResumenDTO.class);
        params.forEach(consultaPagina::setParameter);
        consultaPagina.setFirstResult(pag * tam);   // "saltear" las páginas anteriores
        consultaPagina.setMaxResults(tam);          // "traer solo tam registros"

        List<ClienteResumenDTO> contenido = consultaPagina.getResultList();
        return PaginaDTO.de(contenido, pag, tam, total);
    }

    private boolean hayTexto(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
