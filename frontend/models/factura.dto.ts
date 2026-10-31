
// Tipo literal: solo permite estos dos textos, un typo no compila.
export type EstadoFactura = "EMITIDA" | "ANULADA";

export interface FacturaReporteDTO {
    id: number;
    numeroFactura: number;
    fechaEmision: string;
    clienteDenominacion: string;
    condicionIva: string;
    puntoVentaDescripcion: string;
    importeTotal: number;
    estado: string;
    cantidadItems: number;
}

export interface FacturaDetalleRequestDTO {
    articuloId: number;
    cantidad: number;
    descripcion?: string;
    porcentajeBonificacion: number;
    precioUnitario?: number;
    subtotal?: number;
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
    numero?: number;
    fechaDesde?: string;
    fechaHasta?: string;
    estado?: EstadoFactura;
    montoMinimo?: number;
    clienteId?: number;
}

// ---- Espejo de PaginaDTO<T>.java (T = tipo de cada elemento) ---------------
export interface Pagina<T> {
    contenido: T[];
    pagina: number;                // 0-based: la primera página es la 0
    tamanio: number;
    totalElementos: number;
    totalPaginas: number;
}

// ---- Espejo de ClienteResumenDTO.java --------------------------------------
export interface ClienteResumenDTO {
    id: number;
    denominacion: string;
    cuitCuil: string;
    email: string | null;          // en Java pueden ser null -> union type
    telefono: string | null;
    nombreCalle: string | null;
    numeroCalle: string | null;
}

export interface ClienteFiltrosDTO {
    denominacion?: string;
    cuitCuil?: string;
    contacto?: string;
    domicilio?: string;
}

// ---- Espejo de ArticuloPrecioDTO.java --------------------------------------
export interface ArticuloPrecioDTO {
    listaPrecioArticuloId: number;
    codigo: string;
    denominacion: string;
    precioVenta: number;
}