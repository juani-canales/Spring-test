// Lógica de index.html: filtros, listado paginado, anular y eliminar.
import { FacturaService } from "./FacturaService.js";
import { SelectorCliente } from "./SelectorCliente.js";
import type { FacturaFiltrosDTO, FacturaReporteDTO } from "./models/factura.dto.js";
import {
    crearCelda, formatearFecha, formatearMoneda, leerTexto,
    mensajeDeError, obtener, renderizarPaginacion
} from "./utils.js";

const TAMANIO_PAGINA = 10;

// --------- 1. Elementos del DOM
const servicio = new FacturaService();
const selectorCliente = new SelectorCliente();

const formFiltros = obtener<HTMLFormElement>("#formFiltros");
const cuerpoTabla = obtener<HTMLTableSectionElement>("#tablaFacturas");
const contenedorPaginacion = obtener<HTMLElement>("#paginacionFacturas");
const inputCliente = obtener<HTMLInputElement>("#cliente");
const inputClienteId = obtener<HTMLInputElement>("#clienteId");
const botonBuscar = obtener<HTMLButtonElement>("#btnBuscar");
const cajaMensaje = obtener<HTMLElement>("#mensajeGlobal");

// ----------- 2. Estado en memoria
// Guardamos los filtros de la ÚLTIMA búsqueda para que al cambiar de página
// se sigan aplicando (aunque el usuario haya tocado el formulario sin buscar).
let filtrosActuales: FacturaFiltrosDTO = {};
let paginaActual = 0;

// ------------ 3. Filtros
function leerFiltros(): FacturaFiltrosDTO {
    const datos = new FormData(formFiltros);
    const filtros: FacturaFiltrosDTO = {};

    const numero = leerTexto(datos, "numeroFactura");
    if (numero !== "") filtros.numero = Number(numero);

    const clienteId = leerTexto(datos, "clienteId");
    if (clienteId !== "") filtros.clienteId = Number(clienteId);

    const desde = leerTexto(datos, "fechaDesde");
    if (desde !== "") filtros.fechaDesde = desde;

    const hasta = leerTexto(datos, "fechaHasta");
    if (hasta !== "") filtros.fechaHasta = hasta;

    // "TODAS" no es un estado real: significa "sin filtro", por eso no lo mandamos.
    const estado = leerTexto(datos, "filtroEstado");
    if (estado === "EMITIDA" || estado === "ANULADA") filtros.estado = estado;

    return filtros;
}

// ------------------ 4. Listado + paginación
async function cargarFacturas(pagina: number): Promise<void> {
    botonBuscar.disabled = true;                                  // estado "loading" (Clase 5)
    cuerpoTabla.innerHTML = `<tr><td colspan="7" class="sin-datos">Cargando facturas...</td></tr>`;

    try {
        const resultado = await servicio.obtenerFacturas(filtrosActuales, pagina, TAMANIO_PAGINA);

        // Si borramos el último elemento de la última página, esa página quedó vacía: retrocedemos una.
        if (resultado.contenido.length === 0 && pagina > 0) {
            await cargarFacturas(pagina - 1);
            return;
        }

        paginaActual = resultado.pagina;
        dibujarTabla(resultado.contenido);
        renderizarPaginacion(
            contenedorPaginacion,
            resultado.pagina,
            resultado.totalPaginas,
            resultado.totalElementos,
            (nuevaPagina) => void cargarFacturas(nuevaPagina)
        );
    } catch (error) {
        cuerpoTabla.innerHTML = `<tr><td colspan="7" class="sin-datos">No se pudieron cargar las facturas.</td></tr>`;
        contenedorPaginacion.innerHTML = "";
        mostrarMensaje(mensajeDeError(error), "error");           // estado "error"
    } finally {
        botonBuscar.disabled = false;                             // pase lo que pase, se reactiva
    }
}

function dibujarTabla(facturas: FacturaReporteDTO[]): void {
    cuerpoTabla.innerHTML = "";                                   // limpiamos para no duplicar filas

    if (facturas.length === 0) {
        cuerpoTabla.innerHTML = `<tr><td colspan="7" class="sin-datos">No se encontraron facturas con esos filtros.</td></tr>`;
        return;
    }
    for (const factura of facturas) {
        cuerpoTabla.appendChild(crearFila(factura));
    }
}

function crearFila(factura: FacturaReporteDTO): HTMLTableRowElement {
    const fila = document.createElement("tr");
    fila.append(
        crearCelda(String(factura.numeroFactura)),
        crearCelda(formatearFecha(factura.fechaEmision)),
        crearCelda(factura.clienteDenominacion),
        crearCelda(factura.condicionIva),
        crearCelda(formatearMoneda(factura.importeTotal)),
        crearCelda(factura.estado, `estado-${factura.estado.toLowerCase()}`),
        crearCeldaAcciones(factura)
    );
    return fila;
}

function crearBotonAccion(texto: string, clase: string, alHacerClic: () => void): HTMLButtonElement {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = clase;
    boton.textContent = texto;
    boton.addEventListener("click", alHacerClic);
    return boton;
}

function crearCeldaAcciones(factura: FacturaReporteDTO): HTMLTableCellElement {
    const celda = document.createElement("td");
    celda.className = "acciones-fila";

    if (factura.estado !== "ANULADA") {                           // una anulada no se puede anular otra vez
        celda.appendChild(crearBotonAccion("Anular", "btn-secundario", () => void anular(factura)));
    }
    celda.appendChild(crearBotonAccion("Eliminar", "btn-peligro", () => void eliminar(factura)));
    return celda;
}

// ------------------ 5. Anular / Eliminar
async function anular(factura: FacturaReporteDTO): Promise<void> {
    if (!confirm(`¿Anular la factura N° ${factura.numeroFactura}?`)) return;

    const motivo = prompt("Motivo de la anulación (opcional):");  // string | null
    try {
        await servicio.actualizarEstado(factura.id, "ANULADA", motivo?.trim() || undefined);
        mostrarMensaje(`Factura N° ${factura.numeroFactura} anulada.`, "exito");
        await cargarFacturas(paginaActual);                       // recargamos la MISMA página
    } catch (error) {
        mostrarMensaje(mensajeDeError(error), "error");
    }
}

async function eliminar(factura: FacturaReporteDTO): Promise<void> {
    if (!confirm(`¿Eliminar DEFINITIVAMENTE la factura N° ${factura.numeroFactura}? Esta acción no se puede deshacer.`)) return;

    try {
        await servicio.eliminarFactura(factura.id);
        mostrarMensaje(`Factura N° ${factura.numeroFactura} eliminada.`, "exito");
        await cargarFacturas(paginaActual);
    } catch (error) {
        mostrarMensaje(mensajeDeError(error), "error");
    }
}

// ------------ 6. Mensajes
function mostrarMensaje(texto: string, tipo: "exito" | "error"): void {
    cajaMensaje.textContent = texto;
    cajaMensaje.className = tipo === "exito" ? "mensaje-exito" : "mensaje-validacion";
    cajaMensaje.hidden = false;
}

// ---------- 7. Popover de cliente
async function abrirSelectorCliente(): Promise<void> {
    const cliente = await selectorCliente.elegir();               // la ejecución "espera" acá
    if (cliente === null) return;                                 // el usuario canceló

    inputCliente.value = cliente.denominacion;                    // lo que ve el usuario
    inputClienteId.value = String(cliente.id);                    // lo que se manda al backend
}

inputCliente.addEventListener("click", () => void abrirSelectorCliente());
inputCliente.addEventListener("keydown", (event: KeyboardEvent) => {
    if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();       // Enter dentro de un form lo enviaría; Espacio haría scroll
        void abrirSelectorCliente();
    }
});

// --------- 8. Eventos del formulario
formFiltros.addEventListener("submit", (event: Event) => {
    event.preventDefault();
    filtrosActuales = leerFiltros();
    void cargarFacturas(0);                                       // buscar siempre vuelve a la página 1
});

formFiltros.addEventListener("reset", () => {
    // Un <input type="hidden"> NO se limpia con reset(): lo vaciamos a mano.
    inputClienteId.value = "";
    // El evento 'reset' ocurre ANTES de que los campos se limpien. Con setTimeout(…, 0)
    // esperamos a que el navegador termine de limpiar y recién ahí leemos los filtros.
    setTimeout(() => {
        filtrosActuales = leerFiltros();
        void cargarFacturas(0);
    }, 0);
});

// ------------------ 9. Arranque
// Un <script type="module"> se ejecuta cuando el HTML ya está cargado: no hace falta DOMContentLoaded.
const creada = new URLSearchParams(window.location.search).get("creada");
if (creada !== null) {
    mostrarMensaje(`Factura N° ${creada} creada correctamente.`, "exito");
}

filtrosActuales = leerFiltros();       // al inicio: estado EMITIDA (el valor por defecto del select)
void cargarFacturas(0);
