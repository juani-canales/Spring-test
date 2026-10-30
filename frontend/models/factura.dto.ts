export interface FacturaReporteDTO {
    numeroFactura: number;
    fechaEmision: string;
    clienteDenominacion: string;
    condicionIvaL: number;
    puntoVentaDescripcion: string;
    importeTotal: number;
    cantidadItems: number;
}

export interface FacturaDetalleRequestDTO {
    articuloId: number;
    cantidad: number;
    descripcion?: string;
    porcentajeBonificacion: number;
}

export interface FacturaRequestDTO {
    numero?: number;
    clienteId?: number;
    condicionIvaId: number;
    puntoVentaId: number;
    tipoMonedaId: number;
    importeCobrado?: number;
    observaciones?: string;
    estado?: string;
    detalles: FacturaDetalleRequestDTO[];
}

export interface FacturaFiltrosDTO {
    numeroFactura?: number;
    //¿Está bien así si quiero filtrar por clientes? Por lo que se debe abrir como otra ventana.
    cliente?: string;
    fechaDesde?: string;
    fechaHasta?: string;
    estado?: string;
    montoMinimo?: number;
}