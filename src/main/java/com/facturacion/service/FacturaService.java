package com.facturacion.service;

import com.facturacion.DTO.FacturaDetalleRequestDTO;
import com.facturacion.DTO.FacturaReporteDTO;
import com.facturacion.DTO.FacturaRequestDTO;
import com.facturacion.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FacturaService {

    @PersistenceContext
    EntityManager em;

    //Creación de factura
    @Transactional
    public FacturaReporteDTO crear(FacturaRequestDTO facturaRequestDTO) {
        // Buscar las entidades asociadas por ID

        Cliente cliente = em.find(Cliente.class, facturaRequestDTO.clienteId());
        //No valido si el cliente es null porque si no me lanza un exception y no va a poder ser 'Consumidor Final'

        CondicionIva condicionIva = em.find(CondicionIva.class, facturaRequestDTO.condicionIvaId());
        if (condicionIva == null) {
            throw new EntityNotFoundException("Condicion Iva no encontrado.");
        }
        PuntoVenta puntoVenta = em.find(PuntoVenta.class, facturaRequestDTO.puntoVentaId());
        if (puntoVenta == null) {
            throw new EntityNotFoundException("Punto Venta no encontrado.");
        }
        TipoMoneda tipoMoneda = em.find(TipoMoneda.class, facturaRequestDTO.tipoMonedaId());
        if (tipoMoneda == null) {
            throw new EntityNotFoundException("Tipo de moneda no encontrado.");
        }

        // Crear la factura
        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(facturaRequestDTO.numero());
        factura.setFechaEmision(new Date());
        factura.setPuntoVenta(puntoVenta);
        factura.setCliente(cliente);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(tipoMoneda);
        factura.setImporteCobrado(facturaRequestDTO.importeCobrado());
        factura.setObservaciones(facturaRequestDTO.observaciones());
        factura.setEstado("EMITIDA");

        
        Usuario usuarioAuditoria = em.find(Usuario.class, 1L);
        if (usuarioAuditoria != null) {
            factura.registrarAlta(usuarioAuditoria, new Date());
        }

        // Procesar los detalles, calcular importes netos, IVA y subtotal
        if (facturaRequestDTO.detalles() != null) {
            for (FacturaDetalleRequestDTO detalleDto : facturaRequestDTO.detalles()) {
                // Buscar la listaPrecioArticulo relacinada al artuculo con ese id
                ListaPrecioArticulo lpa = em.find(ListaPrecioArticulo.class, detalleDto.articuloId());

                if (lpa == null) {
                    throw new EntityNotFoundException("ListaPrecioArticulo no encontrado con ID: " + detalleDto.articuloId());
                }

                FacturaVentaDetalle detalle = new FacturaVentaDetalle();
                detalle.setListaPrecioArticulo(lpa);
                detalle.setDescripcion(detalleDto.descripcion() != null ? detalleDto.descripcion() : lpa.getArticulo().getDenominacion());
                detalle.setCantidad(detalleDto.cantidad());
                detalle.setPrecioUnitario(lpa.getPrecioVenta());
                detalle.setPorcentajeBonificacion(detalleDto.porcentajeBonificacion());

                // Cálculo de importes del renglón (ejemplo con IVA 21%)
                double bruto = detalle.getCantidad() * detalle.getPrecioUnitario();
                double descuento = bruto * (detalle.getPorcentajeBonificacion() / 100.0);
                double neto = bruto - descuento;
                double iva = neto * 0.21;
                double subtotal = neto + iva;

                detalle.setImporteNeto(neto);
                detalle.setImporteIva(iva);
                detalle.setImporteSubtotal(subtotal);

                // Vincula bidireccionalmente para que funcione el CascadeType.ALL
                factura.agregarDetalle(detalle);
            }
        }

        // Calcular el total de la factura sumando los subtotales de los detalles
        factura.calcularImportes();

        // Persistir en cascada
        em.persist(factura);
        em.flush();

        // Retornar el FacturaReporteDTO generado
        return FacturaReporteDTO.toEntity(factura);
    }


    // Actualización de factura por su ID
    @Transactional
    public FacturaReporteDTO actualizar(Long id, FacturaRequestDTO dto) {
        FacturaVenta factura = em.find(FacturaVenta.class, id);
        if (factura == null) {
            throw new EntityNotFoundException("Factura no encontrada con ID: " + id);
        }

        // Actualizar relaciones si se envían en el DTO
        if (dto.puntoVentaId() != null) {
            PuntoVenta pv = em.find(PuntoVenta.class, dto.puntoVentaId());
            if (pv == null) throw new EntityNotFoundException("Punto de venta no encontrado");
            factura.setPuntoVenta(pv);
        }
        if (dto.condicionIvaId() != null) {
            CondicionIva ci = em.find(CondicionIva.class, dto.condicionIvaId());
            if (ci == null) throw new EntityNotFoundException("Condición IVA no encontrada");
            factura.setCondicionIva(ci);
        }
        if (dto.tipoMonedaId() != null) {
            TipoMoneda tm = em.find(TipoMoneda.class, dto.tipoMonedaId());
            if (tm == null) throw new EntityNotFoundException("Tipo moneda no encontrado");
            factura.setTipoMoneda(tm);
        }
        if (dto.clienteId() != null) {
            Cliente cl = em.find(Cliente.class, dto.clienteId());
            if (cl == null) throw new EntityNotFoundException("Cliente no encontrado");
            factura.setCliente(cl);
        }

        if (dto.numero() != null) factura.setNumero(dto.numero());
        factura.setImporteCobrado(dto.importeCobrado());
        factura.setObservaciones(dto.observaciones());
        factura.setFechaModificacion(new Date());

        // Actualizar ítems
        if (dto.detalles() != null) {
            factura.getDetalles().clear();
            for (FacturaDetalleRequestDTO detDto : dto.detalles()) {
                ListaPrecioArticulo lpa = em.find(ListaPrecioArticulo.class, detDto.articuloId());
                if (lpa == null) throw new EntityNotFoundException("Artículo no encontrado");

                FacturaVentaDetalle detalle = new FacturaVentaDetalle();
                detalle.setListaPrecioArticulo(lpa);
                detalle.setDescripcion(detDto.descripcion() != null ? detDto.descripcion() : lpa.getArticulo().getDenominacion());
                detalle.setCantidad(detDto.cantidad());
                detalle.setPrecioUnitario(lpa.getPrecioVenta());
                detalle.setPorcentajeBonificacion(detDto.porcentajeBonificacion());

                double bruto = detalle.getCantidad() * detalle.getPrecioUnitario();
                double neto = bruto - (bruto * (detalle.getPorcentajeBonificacion() / 100.0));
                double iva = neto * 0.21;

                detalle.setImporteNeto(neto);
                detalle.setImporteIva(iva);
                detalle.setImporteSubtotal(neto + iva);

                factura.agregarDetalle(detalle);
            }
        }

        factura.calcularImportes();
        em.merge(factura);

        return FacturaReporteDTO.toEntity(factura);
    }


    // Eliminación / Anulación de la factura
    @Transactional
    public void eliminar(Long id) {
        FacturaVenta factura = em.find(FacturaVenta.class, id);
        if (factura == null) {
            throw new EntityNotFoundException("Factura no encontrada con ID: " + id);
        }

        // Anulación lógica
        factura.setEstado("ANULADA");
        factura.setFechaAnulacion(new Date());
        factura.setFechaBaja(new Date());
        em.merge(factura);

        // Eliminación física en la BD
        // em.remove(factura);
    }



    public List<FacturaReporteDTO> buscarFacturasFiltradas(Date fechaDesde, Date fechaHasta, String estado, Double montoMinimo) {
        StringBuilder jpql = new StringBuilder(
                "SELECT new com.facturacion.DTO.FacturaReporteDTO(" +
                        "f.numero, f.fechaEmision, COALESCE(c.denominacion, 'Consumidor Final'), " +
                        "ci.denominacion, pv.descripcion, f.importeTotal, COUNT(d)) " +
                        "FROM FacturaVenta f " +
                        "LEFT JOIN f.cliente c " +
                        "JOIN f.condicionIva ci " +
                        "JOIN f.puntoVenta pv " +
                        "JOIN f.detalles d " +
                        "WHERE 1=1 "
        );

        Map<String, Object> params = new HashMap<>();

        if (fechaDesde != null) {
            jpql.append("AND f.fechaEmision >= :fechaDesde ");
            params.put("fechaDesde", fechaDesde);
        }

        if (fechaHasta != null) {
            jpql.append("AND f.fechaEmision <= :fechaHasta ");
            params.put("fechaHasta", fechaHasta);
        }

        if (estado != null && !estado.trim().isEmpty()) {
            jpql.append("AND f.estado = :estado ");
            params.put("estado", estado);
        }

        if (montoMinimo != null) {
            jpql.append("AND f.importeTotal >= :montoMinimo ");
            params.put("montoMinimo", montoMinimo);
        }

        jpql.append("GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, pv.descripcion, f.importeTotal");

        TypedQuery<FacturaReporteDTO> query = em.createQuery(jpql.toString(), FacturaReporteDTO.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

}
