// Lógica de index.html: filtros, listado paginado, anular y eliminar.
import { FacturaService } from "./FacturaService.js";
import { SelectorCliente } from "./SelectorCliente.js";
import { crearCelda, formatearFecha, formatearMoneda, leerTexto, mensajeDeError, obtener, renderizarPaginacion } from "./utils.js";
const TAMANIO_PAGINA = 5;
// --------- 1. Elementos del DOM
const servicio = new FacturaService();
const selectorCliente = new SelectorCliente();
const formFiltros = obtener("#formFiltros");
const cuerpoTabla = obtener("#tablaFacturas");
const contenedorPaginacion = obtener("#paginacionFacturas");
const inputCliente = obtener("#cliente");
const inputClienteId = obtener("#clienteId");
const botonBuscar = obtener("#btnBuscar");
const cajaMensaje = obtener("#mensajeGlobal");
const selectCondicionIva = obtener("#condicionIva");
const selectFiltroEstado = obtener("#filtroEstado");
// ----------- 2. Estado en memoria
// Guardamos los filtros de la ÚLTIMA búsqueda para que al cambiar de página
// se sigan aplicando (aunque el usuario haya tocado el formulario sin buscar).
let filtrosActuales = {};
let paginaActual = 0;
// ------------ 3. Filtros
function leerFiltros() {
    const datos = new FormData(formFiltros);
    const filtros = {};
    const numero = leerTexto(datos, "numeroFactura");
    if (numero !== "")
        filtros.numero = Number(numero);
    const clienteId = leerTexto(datos, "clienteId");
    if (clienteId !== "")
        filtros.clienteId = Number(clienteId);
    const desde = leerTexto(datos, "fechaDesde");
    if (desde !== "")
        filtros.fechaDesde = desde;
    const hasta = leerTexto(datos, "fechaHasta");
    if (hasta !== "")
        filtros.fechaHasta = hasta;
    // "TODAS" no es un estado real: significa "sin filtro", por eso no lo mandamos.
    const estado = leerTexto(datos, "filtroEstado");
    if (estado === "EMITIDA" || estado === "ANULADA" || estado === "PENDIENTE")
        filtros.estado = estado;
    const condicionIva = leerTexto(datos, "condicionIva");
    if (condicionIva !== "")
        filtros.condicionIvaId = Number(condicionIva);
    return filtros;
}
async function cargarCondicionesIva() {
    try {
        const condiciones = await servicio.obtenerCondicionesIva();
        for (const ci of condiciones) {
            const opcion = document.createElement("option");
            opcion.value = String(ci.id);
            opcion.textContent = ci.denominacion;
            selectCondicionIva.appendChild(opcion);
        }
    }
    catch (error) {
        mostrarMensaje(mensajeDeError(error), "error");
    }
}
// ------------------ 4. Listado + paginación
async function cargarFacturas(pagina) {
    botonBuscar.disabled = true; // estado "loading" (Clase 5)
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
        renderizarPaginacion(contenedorPaginacion, resultado.pagina, resultado.totalPaginas, resultado.totalElementos, (nuevaPagina) => void cargarFacturas(nuevaPagina));
    }
    catch (error) {
        cuerpoTabla.innerHTML = `<tr><td colspan="7" class="sin-datos">No se pudieron cargar las facturas.</td></tr>`;
        contenedorPaginacion.innerHTML = "";
        mostrarMensaje(mensajeDeError(error), "error"); // estado "error"
    }
    finally {
        botonBuscar.disabled = false; // pase lo que pase, se reactiva
    }
}
function dibujarTabla(facturas) {
    cuerpoTabla.innerHTML = ""; // limpiamos para no duplicar filas
    if (facturas.length === 0) {
        cuerpoTabla.innerHTML = `<tr><td colspan="7" class="sin-datos">No se encontraron facturas con esos filtros.</td></tr>`;
        return;
    }
    for (const factura of facturas) {
        cuerpoTabla.appendChild(crearFila(factura));
    }
}
function crearFila(factura) {
    const fila = document.createElement("tr");
    fila.append(crearCelda(factura.numeroFactura !== null ? String(factura.numeroFactura) : "—"), crearCelda(formatearFecha(factura.fechaEmision)), crearCelda(factura.clienteDenominacion), crearCelda(factura.condicionIva), crearCelda(formatearMoneda(factura.importeTotal)), crearCelda(factura.estado, `estado-${factura.estado.toLowerCase()}`), crearCeldaAcciones(factura));
    return fila;
}
function crearBotonAccion(texto, clase, alHacerClic) {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = clase;
    boton.textContent = texto;
    boton.addEventListener("click", alHacerClic);
    return boton;
}
function crearCeldaAcciones(factura) {
    const celda = document.createElement("td");
    celda.className = "acciones-fila";
    // Solo una factura PENDIENTE (nunca informada a ARCA) admite acciones.
    // Una EMITIDA tiene CAE: no se edita, no se anula y no se borra (se corrige con nota de crédito).
    if (factura.estado === "PENDIENTE") {
        celda.appendChild(crearBotonAccion("Editar", "btn-editor", () => {
            window.location.href = `nueva-factura.html?id=${factura.id}`;
        }));
        celda.appendChild(crearBotonAccion("Anular", "btn-anulado", () => void anular(factura)));
        celda.appendChild(crearBotonAccion("Eliminar", "btn-peligro", () => void eliminar(factura)));
    }
    else {
        celda.textContent = "—";
    }
    return celda;
}
// ------------------ 5. Anular / Eliminar
async function anular(factura) {
    if (!confirm(`¿Anular la factura ${etiquetaNumero(factura)}?`))
        return;
    const motivo = prompt("Motivo de la anulación (opcional):"); // string | null
    try {
        await servicio.actualizarEstado(factura.id, "ANULADA", motivo?.trim() || undefined);
        mostrarMensaje(`Factura N° ${etiquetaNumero(factura)} anulada.`, "exito");
        await cargarFacturas(paginaActual); // recargamos la MISMA página
    }
    catch (error) {
        mostrarMensaje(mensajeDeError(error), "error");
    }
}
async function eliminar(factura) {
    if (!confirm(`¿Eliminar DEFINITIVAMENTE la factura N° ${etiquetaNumero(factura)}? Esta acción no se puede deshacer.`))
        return;
    try {
        await servicio.eliminarFactura(factura.id);
        mostrarMensaje(`Factura N° ${etiquetaNumero(factura)} eliminada.`, "exito");
        await cargarFacturas(paginaActual);
    }
    catch (error) {
        mostrarMensaje(mensajeDeError(error), "error");
    }
}
// ------------ 6. Mensajes
function mostrarMensaje(texto, tipo) {
    cajaMensaje.textContent = texto;
    cajaMensaje.className = tipo === "exito" ? "mensaje-exito" : "mensaje-validacion";
    cajaMensaje.hidden = false;
}
// ---------- 7. Popover de cliente
async function abrirSelectorCliente() {
    const cliente = await selectorCliente.elegir(); // la ejecución "espera" acá
    if (cliente === null)
        return; // el usuario canceló
    inputCliente.value = cliente.denominacion; // lo que ve el usuario
    inputClienteId.value = String(cliente.id); // lo que se manda al backend
}
inputCliente.addEventListener("click", () => void abrirSelectorCliente());
inputCliente.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") {
        event.preventDefault(); // Enter dentro de un form lo enviaría; Espacio haría scroll
        void abrirSelectorCliente();
    }
});
// --------- 8. Eventos del formulario
formFiltros.addEventListener("submit", (event) => {
    event.preventDefault();
    filtrosActuales = leerFiltros();
    void cargarFacturas(0); // buscar siempre vuelve a la página 1
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
const parametros = new URLSearchParams(window.location.search);
const informada = parametros.get("informada");
const pendiente = parametros.get("pendiente");
if (informada !== null) {
    mostrarMensaje(`Factura N° ${informada} informada a ARCA correctamente.`, "exito");
}
else if (pendiente !== null) {
    mostrarMensaje("Factura guardada como PENDIENTE. Podés editarla e informarla a ARCA cuando quieras.", "exito");
    selectFiltroEstado.value = "PENDIENTE"; // si no, el filtro por defecto (Emitidas) no la mostraría
}
filtrosActuales = leerFiltros(); // después de tocar el select, para que tome el valor nuevo
void cargarCondicionesIva();
void cargarFacturas(0);
//---------
function etiquetaNumero(factura) {
    return factura.numeroFactura !== null ? `N° ${factura.numeroFactura}` : "(pendiente, sin número)";
}
