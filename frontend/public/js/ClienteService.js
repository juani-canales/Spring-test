import { verificarRespuesta } from "./utils.js";
const API_CLIENTES = "http://localhost:8080/api/clientes";
export class ClienteService {
    // GET /api/clientes?denominacion=juan&pagina=0&tamanio=5
    async buscarClientes(filtros, pagina = 0, tamanio = 5) {
        const params = new URLSearchParams();
        params.set("pagina", String(pagina));
        params.set("tamanio", String(tamanio));
        if (filtros.denominacion)
            params.set("denominacion", filtros.denominacion);
        if (filtros.cuitCuil)
            params.set("cuitCuil", filtros.cuitCuil);
        if (filtros.contacto)
            params.set("contacto", filtros.contacto);
        if (filtros.domicilio)
            params.set("domicilio", filtros.domicilio);
        const respuesta = await fetch(`${API_CLIENTES}?${params.toString()}`);
        await verificarRespuesta(respuesta);
        const datos = await respuesta.json();
        return datos;
    }
}
