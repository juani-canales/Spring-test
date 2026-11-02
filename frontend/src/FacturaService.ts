import type {
    ArticuloPrecioDTO,
    CondicionIvaDTO,
    EstadoFactura,
    FacturaEdicionDTO,
    FacturaFiltrosDTO,
    FacturaReporteDTO,
    FacturaRequestDTO,
    ListaPrecioDTO,
    Pagina,
    PuntoVentaDTO
} from "./models/factura.dto.js";
import { verificarRespuesta } from "./utils.js";

const API = "http://localhost:8080/api/facturas";

export class FacturaService {

    // GET /api/facturas?numero=14&estado=EMITIDA&pagina=0&tamanio=10
    async obtenerFacturas(
        filtros: FacturaFiltrosDTO,
        pagina: number = 0,
        tamanio: number = 10
    ): Promise<Pagina<FacturaReporteDTO>> {

        // URLSearchParams arma la parte "?clave=valor&clave=valor" y escapa los caracteres raros.
        const params = new URLSearchParams();
        params.set("pagina", String(pagina));
        params.set("tamanio", String(tamanio));

        // Solo mandamos los filtros que el usuario realmente completó.
        if (filtros.numero !== undefined) params.set("numero", String(filtros.numero));
        if (filtros.clienteId !== undefined) params.set("clienteId", String(filtros.clienteId));
        if (filtros.condicionIvaId !== undefined) params.set("condicionIvaId", String(filtros.condicionIvaId));
        if (filtros.fechaDesde) params.set("fechaDesde", filtros.fechaDesde);
        if (filtros.fechaHasta) params.set("fechaHasta", filtros.fechaHasta);
        if (filtros.estado) params.set("estado", filtros.estado);
        if (filtros.montoMinimo !== undefined) params.set("montoMinimo", String(filtros.montoMinimo));

        const respuesta = await fetch(`${API}?${params.toString()}`);
        await verificarRespuesta(respuesta);

        const datos: Pagina<FacturaReporteDTO> = await respuesta.json();
        return datos;
    }

    // POST /api/facturas  ->  201 Created + FacturaReporteDTO
    async crearFactura(factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {
        const respuesta = await fetch(API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },   // avisamos que el cuerpo es JSON
            body: JSON.stringify(factura)                       // objeto -> texto JSON
        });
        await verificarRespuesta(respuesta);

        const datos: FacturaReporteDTO = await respuesta.json();
        return datos;
    }

    // PUT /api/facturas/{id}  ->  200 OK + FacturaReporteDTO
    // Mandamos un PUT "parcial": solo estado y observaciones (Partial = todos los campos opcionales).
    async actualizarEstado(
        id: number,
        estado: EstadoFactura,
        observaciones?: string
    ): Promise<FacturaReporteDTO> {
        const cambios: Partial<FacturaRequestDTO> = { estado, observaciones };

        const respuesta = await fetch(`${API}/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cambios)    // JSON.stringify omite los campos undefined
        });
        await verificarRespuesta(respuesta);

        const datos: FacturaReporteDTO = await respuesta.json();
        return datos;
    }

    // NUEVO: PUT /api/facturas/{id} con lista de precios + renglones (edición de artículos)
    async actualizarFactura(
        id: number,
        cambios: Partial<FacturaRequestDTO>
    ): Promise<FacturaReporteDTO> {
        const respuesta = await fetch(`${API}/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cambios)
        });
        await verificarRespuesta(respuesta);

        const datos: FacturaReporteDTO = await respuesta.json();
        return datos;
    }

    // NUEVO: GET /api/facturas/{id} -> datos completos para precargar la pantalla de edición
    async obtenerFacturaParaEdicion(id: number): Promise<FacturaEdicionDTO> {
        const respuesta = await fetch(`${API}/${id}`);
        await verificarRespuesta(respuesta);

        const datos: FacturaEdicionDTO = await respuesta.json();
        return datos;
    }

    // DELETE /api/facturas/{id}  ->  204 No Content (no hay cuerpo, NO se llama a .json())
    async eliminarFactura(id: number): Promise<void> {
        const respuesta = await fetch(`${API}/${id}`, { method: "DELETE" });
        await verificarRespuesta(respuesta);
    }

    // NUEVO: GET /api/facturas/listas-precio -> opciones del <select> de lista de precios
    async obtenerListasPrecio(): Promise<ListaPrecioDTO[]> {
        const respuesta = await fetch(`${API}/listas-precio`);
        await verificarRespuesta(respuesta);

        const datos: ListaPrecioDTO[] = await respuesta.json();
        return datos;
    }

    // NUEVO: GET /api/facturas/puntos-venta -> opciones del <select> de punto de venta
    async obtenerPuntosVenta(): Promise<PuntoVentaDTO[]> {
        const respuesta = await fetch(`${API}/puntos-venta`);
        await verificarRespuesta(respuesta);

        const datos: PuntoVentaDTO[] = await respuesta.json();
        return datos;
    }

    // NUEVO: GET /api/facturas/condiciones-iva -> opciones del <select> de condición IVA
    async obtenerCondicionesIva(): Promise<CondicionIvaDTO[]> {
        const respuesta = await fetch(`${API}/condiciones-iva`);
        await verificarRespuesta(respuesta);

        const datos: CondicionIvaDTO[] = await respuesta.json();
        return datos;
    }

    // CAMBIÓ: GET /api/facturas/articulos?listaPrecioId=1 -> solo los artículos de ESA lista
    async obtenerArticulos(listaPrecioId: number): Promise<ArticuloPrecioDTO[]> {
        const respuesta = await fetch(`${API}/articulos?listaPrecioId=${listaPrecioId}`);
        await verificarRespuesta(respuesta);

        const datos: ArticuloPrecioDTO[] = await respuesta.json();
        return datos;
    }
}