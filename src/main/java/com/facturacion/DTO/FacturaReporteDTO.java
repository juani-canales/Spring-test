package com.facturacion.DTO;

import com.facturacion.entities.FacturaVenta;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FacturaReporteDTO {

    private Long numeroFactura;
    private Date fechaEmision;
    private String clienteDenominacion;
    private String condicionIva;
    private String puntoVentaDescripcion;
    private double importeTotal;
    private long cantidadItems;

    public static FacturaReporteDTO toEntity(FacturaVenta factura){
        String clienteDenom = (factura.getCliente() != null) ? factura.getCliente().getDenominacion() : "Consumidor Final";
        return new FacturaReporteDTO(
                factura.getNumero(),
                factura.getFechaEmision(),
                clienteDenom,
                factura.getCondicionIva().getDenominacion(),
                factura.getPuntoVenta().getDescripcion(),
                factura.getImporteTotal(),
                factura.getDetalles().size()
        );
    }
}