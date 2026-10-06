package com.facturacion.service;

import com.facturacion.DTO.FacturaReporteDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FacturaReporteService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<FacturaReporteDTO> obtenerReporte() {

        String jpql =
                "SELECT new com.ejemplo.dto.FacturaReporteDTO(" +
                        " f.numero, " +
                        " f.fechaEmision, " +
                        " COALESCE(c.denominacion, 'Consumidor Final'), " +
                        " ci.denominacion, " +
                        " pv.descripcion, " +
                        " f.importeTotal, " +
                        " COUNT(d)" +
                        ") " +
                        "FROM FacturaVenta f " +
                        "LEFT JOIN f.cliente c " +
                        "JOIN f.condicionIva ci " +
                        "JOIN f.puntoVenta pv " +
                        "JOIN f.detalles d " +
                        "GROUP BY f.id, f.numero, f.fechaEmision, " +
                        "c.denominacion, ci.denominacion, " +
                        "pv.descripcion, f.importeTotal";

        return entityManager
                .createQuery(jpql, FacturaReporteDTO.class)
                .getResultList();
    }
}