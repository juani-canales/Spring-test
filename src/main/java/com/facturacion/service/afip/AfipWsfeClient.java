package com.facturacion.service.afip;

import com.afip.wsfe.*;

public class AfipWsfeClient {

    /** Resultado simplificado para que el resto del sistema no dependa de las clases de ARCA. */
    public record ResultadoCae(boolean aprobado, String cae, String vencimientoCae, String mensajes) {}

    private final ServiceSoap servicio;
    private final long cuit;

    public AfipWsfeClient(long cuit) {
        this.cuit = cuit;
        this.servicio = new Service().getServiceSoap();
    }

    private FEAuthRequest auth(String token, String sign) {
        FEAuthRequest a = new FEAuthRequest();
        a.setToken(token);
        a.setSign(sign);
        a.setCuit(cuit);
        return a;
    }

    /** PASO 2: pregunta el último comprobante autorizado y devuelve el siguiente número. */
    public long proximoNumero(String token, String sign, int ptoVta, int cbteTipo) {
        FERecuperaLastCbteResponse resp =
                servicio.feCompUltimoAutorizado(auth(token, sign), ptoVta, cbteTipo);
        return resp.getCbteNro() + 1L;
    }

    /**
     * PASO 3: solicita el CAE.
     * @param fecha        formato yyyyMMdd
     * @param docTipo      99 = Consumidor Final, 80 = CUIT
     * @param docNro       0 si es Consumidor Final; si no, el CUIT sin guiones
     * @param condIvaReceptor código ARCA de la condición IVA del cliente (5 = Consumidor Final, etc.)
     * @param neto, iva    importes ya redondeados a 2 decimales
     */
    public ResultadoCae solicitarCae(String token, String sign, int ptoVta, int cbteTipo,
                                     long numero, String fecha, int docTipo, long docNro,
                                     int condIvaReceptor, double neto, double iva) {

        boolean facturaC = (cbteTipo == 11);   // la C no discrimina IVA
        double impNeto = facturaC ? neto + iva : neto;
        double impIva  = facturaC ? 0.0 : iva;
        double total   = neto + iva;

        // --- Cabecera
        FECAECabRequest cab = new FECAECabRequest();
        cab.setCantReg(1);
        cab.setPtoVta(ptoVta);
        cab.setCbteTipo(cbteTipo);

        // --- Detalle (una sola factura)
        FECAEDetRequest det = new FECAEDetRequest();
        det.setConcepto(1);                    // 1 = Productos
        det.setDocTipo(docTipo);
        det.setDocNro(docNro);
        det.setCbteDesde(numero);
        det.setCbteHasta(numero);
        det.setCbteFch(fecha);
        det.setImpTotal(redondear(total));
        det.setImpTotConc(0.0);
        det.setImpNeto(redondear(impNeto));
        det.setImpOpEx(0.0);
        det.setImpTrib(0.0);
        det.setImpIVA(redondear(impIva));
        det.setMonId("PES");                   // pesos argentinos
        det.setMonCotiz(1.0);
        det.setCondicionIVAReceptorId(condIvaReceptor); // ver nota abajo

        if (!facturaC) {
            AlicIva alicuota = new AlicIva();
            alicuota.setId(5);                 // 5 = 21 %
            alicuota.setBaseImp(redondear(neto));
            alicuota.setImporte(redondear(iva));
            ArrayOfAlicIva ivas = new ArrayOfAlicIva();
            ivas.getAlicIva().add(alicuota);
            det.setIva(ivas);
        }

        ArrayOfFECAEDetRequest detalles = new ArrayOfFECAEDetRequest();
        detalles.getFECAEDetRequest().add(det);

        FECAERequest request = new FECAERequest();
        request.setFeCabReq(cab);
        request.setFeDetReq(detalles);

        // --- Llamada y lectura de la respuesta
        FECAEResponse resp = servicio.fecaeSolicitar(auth(token, sign), request);

        StringBuilder mensajes = new StringBuilder();
        if (resp.getErrors() != null) {
            resp.getErrors().getErr().forEach(e ->
                    mensajes.append("[").append(e.getCode()).append("] ").append(e.getMsg()).append(" "));
        }
        FECAEDetResponse r = resp.getFeDetResp().getFECAEDetResponse().get(0);
        if (r.getObservaciones() != null) {
            r.getObservaciones().getObs().forEach(o ->
                    mensajes.append("[").append(o.getCode()).append("] ").append(o.getMsg()).append(" "));
        }

        boolean aprobado = resp.getFeCabResp() != null && "A".equals(resp.getFeCabResp().getResultado());
        return new ResultadoCae(aprobado, r.getCAE(), r.getCAEFchVto(), mensajes.toString().trim());
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}