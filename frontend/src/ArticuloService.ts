import type { ArticuloFiltrosDTO, ArticuloPrecioDTO, Pagina } from "./models/factura.dto.js";
import { verificarRespuesta } from "./utils.js";

const API_ARTICULOS = "http://localhost:8080/api/articulos";

export class ArticuloService {

    // GET /api/articulos?listaPrecioId=1&marca=log&precioMinimo=5000&pagina=0&tamanio=5
    async buscarArticulos(
        listaPrecioId: number,
        filtros: ArticuloFiltrosDTO,
        pagina: number = 0,
        tamanio: number = 5
    ): Promise<Pagina<ArticuloPrecioDTO>> {
        const params = new URLSearchParams();
        params.set("listaPrecioId", String(listaPrecioId));
        params.set("pagina", String(pagina));
        params.set("tamanio", String(tamanio));

        // Solo mandamos los filtros que el usuario realmente completó.
        if (filtros.codigo) params.set("codigo", filtros.codigo);
        if (filtros.denominacion) params.set("denominacion", filtros.denominacion);
        if (filtros.marca) params.set("marca", filtros.marca);
        if (filtros.rubro) params.set("rubro", filtros.rubro);
        // precioMinimo = 0 es válido, por eso se compara con undefined y no con "truthy"
        if (filtros.precioMinimo !== undefined) params.set("precioMinimo", String(filtros.precioMinimo));

        const respuesta = await fetch(`${API_ARTICULOS}?${params.toString()}`);
        await verificarRespuesta(respuesta);

        const datos: Pagina<ArticuloPrecioDTO> = await respuesta.json();
        return datos;
    }
}