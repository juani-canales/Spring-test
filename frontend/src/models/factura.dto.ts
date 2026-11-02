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
    listaPrecioId?: number;          // NUEVO: opcional acá porque el PUT parcial (edición) puede omitirlo
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
    condicionIvaId?: number;
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

// ---- NUEVO: espejo de ListaPrecioDTO.java ----------------------------------
export interface ListaPrecioDTO {
    id: number;
    codigo: string;
    denominacion: string;
}

// ---- NUEVO: espejo de FacturaItemEdicionDTO.java ---------------------------
export interface FacturaItemEdicionDTO {
    listaPrecioArticuloId: number;
    codigo: string;
    denominacion: string;
    precioUnitario: number;
    cantidad: number;
    porcentajeBonificacion: number;
}

// ---- NUEVO: espejo de FacturaEdicionDTO.java -------------------------------
export interface FacturaEdicionDTO {
    id: number;
    numero: number;
    fechaEmision: string;
    puntoVentaId: number;
    puntoVentaDescripcion: string;
    clienteId: number | null;          // null = Consumidor Final
    clienteDenominacion: string;
    condicionIvaId: number;
    condicionIvaDenominacion: string;
    listaPrecioId: number | null;      // se deduce de los renglones; null = la factura no tiene renglones
    listaPrecioDescripcion: string | null;
    estado: string;
    detalles: FacturaItemEdicionDTO[];
}

// --- Espejo de PuntoVentaDTO.java
export interface PuntoVentaDTO {
    id: number;
    numero: number;
    descripcion: string;
}

// --- Espejo de CondicionIvaDTO.java
export interface CondicionIvaDTO {
    id: number;
    denominacion: string;
}