package com.facturacion.service;

import com.facturacion.DTO.*;
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

    private static final double ALICUOTA_IVA = 0.21;

    @PersistenceContext
    EntityManager em;

    // ------ CREAR
    @Transactional
    public FacturaReporteDTO crear(FacturaRequestDTO dto) {

        // El cliente es OPCIONAL (si no viene, es "Consumidor Final").
        // Ojo: em.find(Cliente.class, null) lanza IllegalArgumentException, por eso
        // solo buscamos si el id no es null.
        Cliente cliente = null;
        if (dto.clienteId() != null) {
            cliente = buscarObligatorio(Cliente.class, dto.clienteId(), "Cliente");
        }
        CondicionIva condicionIva = buscarObligatorio(CondicionIva.class, dto.condicionIvaId(), "Condición IVA");
        PuntoVenta puntoVenta = buscarObligatorio(PuntoVenta.class, dto.puntoVentaId(), "Punto de venta");
        TipoMoneda tipoMoneda = buscarObligatorio(TipoMoneda.class, dto.tipoMonedaId(), "Tipo de moneda");

        FacturaVenta factura = new FacturaVenta();
        factura.setNumero(dto.numero());
        factura.setFechaEmision(new Date());          // la fecha de emisión la fija el servidor
        factura.setPuntoVenta(puntoVenta);
        factura.setCliente(cliente);
        factura.setCondicionIva(condicionIva);
        factura.setTipoMoneda(tipoMoneda);
        // importeCobrado es Double (puede ser null) pero la entidad usa double: evitamos el NullPointerException
        factura.setImporteCobrado(dto.importeCobrado() != null ? dto.importeCobrado() : 0.0);
        factura.setObservaciones(dto.observaciones());
        factura.setEstado("EMITIDA");

        Usuario usuarioAuditoria = em.find(Usuario.class, 1L);
        if (usuarioAuditoria != null) {
            factura.registrarAlta(usuarioAuditoria, new Date());
        }

        if (dto.detalles() != null) {
            for (FacturaDetalleRequestDTO detalleDto : dto.detalles()) {
                factura.agregarDetalle(construirDetalle(detalleDto));
            }
        }

        factura.calcularImportes();
        em.persist(factura);   // CascadeType.ALL persiste también los detalles
        em.flush();

        return FacturaReporteDTO.toEntity(factura);
    }

    // ------- ACTUALIZAR
    @Transactional
    public FacturaReporteDTO actualizar(Long id, FacturaRequestDTO dto) {
        FacturaVenta factura = buscarObligatorio(FacturaVenta.class, id, "Factura");

        if (dto.puntoVentaId() != null) {
            factura.setPuntoVenta(buscarObligatorio(PuntoVenta.class, dto.puntoVentaId(), "Punto de venta"));
        }
        if (dto.condicionIvaId() != null) {
            factura.setCondicionIva(buscarObligatorio(CondicionIva.class, dto.condicionIvaId(), "Condición IVA"));
        }
        if (dto.tipoMonedaId() != null) {
            factura.setTipoMoneda(buscarObligatorio(TipoMoneda.class, dto.tipoMonedaId(), "Tipo de moneda"));
        }
        if (dto.clienteId() != null) {
            factura.setCliente(buscarObligatorio(Cliente.class, dto.clienteId(), "Cliente"));
        }

        // Solo tocamos lo que viene en el DTO (así el front puede mandar un PUT parcial)
        if (dto.numero() != null) factura.setNumero(dto.numero());
        if (dto.importeCobrado() != null) factura.setImporteCobrado(dto.importeCobrado());
        if (dto.observaciones() != null) factura.setObservaciones(dto.observaciones());

        // Cambio de estado (ej: anulación lógica)
        if (dto.estado() != null) {
            factura.setEstado(dto.estado());
            if ("ANULADA".equals(dto.estado())) {
                factura.setFechaAnulacion(new Date());
            }
        }
        factura.setFechaModificacion(new Date());

        if (dto.detalles() != null) {
            factura.getDetalles().clear();     // requiere orphanRemoval = true en FacturaVenta.detalles
            for (FacturaDetalleRequestDTO detDto : dto.detalles()) {
                factura.agregarDetalle(construirDetalle(detDto));
            }
            factura.calcularImportes();
        }

        return FacturaReporteDTO.toEntity(factura);
    }

    // ------ ELIMINAR
    // Decisión de negocio: "Anular" (PUT estado=ANULADA) es la baja LÓGICA: la factura queda en la BD.
    // "Eliminar" (DELETE) es la baja FÍSICA: se borra la factura y sus detalles (cascade).
    @Transactional
    public void eliminar(Long id) {
        FacturaVenta factura = buscarObligatorio(FacturaVenta.class, id, "Factura");
        em.remove(factura);
    }


    @Transactional(readOnly = true)
    public PaginaDTO<FacturaReporteDTO> buscarFacturasFiltradas(Long numero, Long clienteId, Date fechaDesde,
                                                                Date fechaHasta, String estado, Double montoMinimo,
                                                                int pagina, int tamanio) {
        // Protegemos al backend de valores raros (página negativa, tamanio = 100000...)
        int pag = Math.max(pagina, 0);
        int tam = Math.min(Math.max(tamanio, 1), 100);

        String desde = " FROM FacturaVenta f " +
                "LEFT JOIN f.cliente c " +
                "JOIN f.condicionIva ci " +
                "JOIN f.puntoVenta pv ";

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (numero != null) {
            where.append("AND f.numero = :numero ");
            params.put("numero", numero);
        }
        if (clienteId != null) {
            where.append("AND c.id = :clienteId ");
            params.put("clienteId", clienteId);
        }
        if (fechaDesde != null) {
            where.append("AND f.fechaEmision >= :fechaDesde ");
            params.put("fechaDesde", fechaDesde);
        }
        if (fechaHasta != null) {
            where.append("AND f.fechaEmision <= :fechaHasta ");
            params.put("fechaHasta", fechaHasta);
        }
        if (estado != null && !estado.trim().isEmpty()) {
            where.append("AND f.estado = :estado ");
            params.put("estado", estado);
        }
        if (montoMinimo != null) {
            where.append("AND f.importeTotal >= :montoMinimo ");
            params.put("montoMinimo", montoMinimo);
        }

        // 1) Total de facturas que cumplen los filtros (para calcular cuántas páginas hay)
        TypedQuery<Long> consultaTotal = em.createQuery("SELECT COUNT(f)" + desde + where, Long.class);
        params.forEach(consultaTotal::setParameter);
        long total = consultaTotal.getSingleResult();

        // 2) Solo las facturas de la página pedida
        String jpql = "SELECT new com.facturacion.DTO.FacturaReporteDTO(" +
                "f.id, f.numero, f.fechaEmision, COALESCE(c.denominacion, 'Consumidor Final'), " +
                "ci.denominacion, pv.descripcion, f.importeTotal, f.estado, COUNT(d)) " +
                desde +
                "LEFT JOIN f.detalles d " +
                where +
                "GROUP BY f.id, f.numero, f.fechaEmision, c.denominacion, ci.denominacion, " +
                "pv.descripcion, f.importeTotal, f.estado " +
                "ORDER BY f.fechaEmision DESC, f.id DESC";

        TypedQuery<FacturaReporteDTO> consultaPagina = em.createQuery(jpql, FacturaReporteDTO.class);
        params.forEach(consultaPagina::setParameter);
        consultaPagina.setFirstResult(pag * tam);       // "saltear" las páginas anteriores
        consultaPagina.setMaxResults(tam);              // "traer solo tam registros"

        return PaginaDTO.de(consultaPagina.getResultList(), pag, tam, total);
    }

    //  CATÁLOGO DE ARTÍCULOS
    @Transactional(readOnly = true)
    public List<ArticuloPrecioDTO> listarArticulosConPrecio() {
        return em.createQuery(
                "SELECT new com.facturacion.DTO.ArticuloPrecioDTO(lpa.id, a.codigo, a.denominacion, lpa.precioVenta) " +
                        "FROM ListaPrecioArticulo lpa JOIN lpa.articulo a " +
                        "WHERE lpa.fechaBaja IS NULL " +
                        "ORDER BY a.denominacion",
                ArticuloPrecioDTO.class).getResultList();
    }

    // ------ PRIVADOS
    /** Busca una entidad por id; si el id es null o no existe lanza EntityNotFoundException (-> 404). */
    private <T> T buscarObligatorio(Class<T> clase, Long id, String nombre) {
        T entidad = (id != null) ? em.find(clase, id) : null;
        if (entidad == null) {
            throw new EntityNotFoundException(nombre + " no encontrado/a con ID: " + id);
        }
        return entidad;
    }

    /** Arma un renglón de factura y calcula neto, IVA y subtotal (antes estaba duplicado en crear y actualizar). */
    private FacturaVentaDetalle construirDetalle(FacturaDetalleRequestDTO dto) {
        // Ojo: dto.articuloId() es el ID de ListaPrecioArticulo (el precio vigente del artículo)
        ListaPrecioArticulo lpa = buscarObligatorio(ListaPrecioArticulo.class, dto.articuloId(), "Artículo con precio");

        FacturaVentaDetalle detalle = new FacturaVentaDetalle();
        detalle.setListaPrecioArticulo(lpa);
        detalle.setDescripcion(dto.descripcion() != null ? dto.descripcion() : lpa.getArticulo().getDenominacion());
        detalle.setCantidad(dto.cantidad());
        detalle.setPrecioUnitario(lpa.getPrecioVenta());   // el precio lo decide el servidor, no el cliente
        detalle.setPorcentajeBonificacion(dto.porcentajeBonificacion());

        double bruto = detalle.getCantidad() * detalle.getPrecioUnitario();
        double neto = bruto - bruto * (detalle.getPorcentajeBonificacion() / 100.0);
        double iva = neto * ALICUOTA_IVA;

        detalle.setImporteNeto(neto);
        detalle.setImporteIva(iva);
        detalle.setImporteSubtotal(neto + iva);
        return detalle;
    }

}
