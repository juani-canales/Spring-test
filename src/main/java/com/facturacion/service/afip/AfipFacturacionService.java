package com.facturacion.service.afip;

import com.facturacion.entities.FacturaVenta;
import com.facturacion.entities.FacturaVentaDetalle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;

@Service
public class AfipFacturacionService {

    @Value("${afip.cuit}")
    private long cuit;

    @Value("${afip.key-path}")
    private String keyPath;

    @Value("${afip.cert-path}")
    private String certPath;

    @Value("${afip.tipo-comprobante}")
    private int cbteTipo;

    private String token, sign;
    private Instant venceToken = Instant.EPOCH;

    private synchronized String[] credenciales() throws Exception {
        return AfipWsaaClient.obtenerTokenYSignConCache(keyPath, certPath,
                "C:/Users/canal/OneDrive/Escritorio/ARCAico/ta-wsfe.cache");
    }

    /** Pide el CAE y completa los datos de ARCA en la factura. Lanza excepción si ARCA la rechaza. */
    public void autorizar(FacturaVenta factura) {
        try {
            String[] cred = credenciales();
            AfipWsfeClient wsfe = new AfipWsfeClient(cuit);
            int ptoVta = factura.getPuntoVenta().getNumero();

            long numero = wsfe.proximoNumero(cred[0], cred[1], ptoVta, cbteTipo);

            double neto = 0, iva = 0;
            for (FacturaVentaDetalle d : factura.getDetalles()) {
                neto += d.getImporteNeto();
                iva += d.getImporteIva();
            }

            int docTipo = 99;      // Consumidor Final
            long docNro = 0;
            if (factura.getCliente() != null) {
                docTipo = 80;      // CUIT
                docNro = Long.parseLong(factura.getCliente().getCuitCuil().replaceAll("\\D", ""));
            }

            String fecha = new SimpleDateFormat("yyyyMMdd").format(factura.getFechaEmision());
            int condIva = factura.getCondicionIva().getCodigoAfip();

            var res = wsfe.solicitarCae(cred[0], cred[1], ptoVta, cbteTipo, numero,
                    fecha, docTipo, docNro, condIva, neto, iva);

            if (!res.aprobado()) {
                factura.setResultadoAfip("R");
                throw new IllegalArgumentException("ARCA rechazó la factura: " + res.mensajes());
            }

            factura.setNumero(numero);            // el número lo da ARCA
            factura.setCae(res.cae());
            factura.setCaeFechaVencimiento(new SimpleDateFormat("yyyyMMdd").parse(res.vencimientoCae()));
            factura.setResultadoAfip("A");
            factura.setMotivoRechazo(res.mensajes().isEmpty() ? null : res.mensajes());

        } catch (IllegalArgumentException e) {
            throw e;                              // rechazo de ARCA: llega como 400 al front
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo comunicar con ARCA: " + e.getMessage(), e);
        }
    }
}