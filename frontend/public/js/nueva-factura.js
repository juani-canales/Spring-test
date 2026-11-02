// Lógica de nueva-factura.html. Esta MISMA pantalla sirve para dos modos:
//   - ALTA:     nueva-factura.html           (sin parámetros)
//   - EDICIÓN:  nueva-factura.html?id=12     (con el id de la factura a editar)
import { FacturaService } from "./FacturaService.js";
import { SelectorCliente } from "./SelectorCliente.js";
import { fechaDeHoy, formatearMoneda, leerTexto, mensajeDeError, obtener } from "./utils.js";
const ALICUOTA_IVA = 0.21; // el backend usa el mismo 21%: acá es solo una vista previa
// ---------------------------------------------------------------- Modo (alta o edición)
// Si la URL trae ?id=12 estamos editando; si no, estamos creando.
const idParam = new URLSearchParams(window.location.search).get("id");
const facturaId = idParam !== null ? Number(idParam) : null;
const modoEdicion = facturaId !== null;
const textoBotonGuardar = modoEdicion ? "Guardar cambios" : "Guardar factura";
// ---------------------------------------------------------------- Elementos DOM
const servicio = new FacturaService();
const selectorCliente = new SelectorCliente();
const formulario = obtener("#formNuevaFactura");
const titulo = obtener("#tituloPagina");
const subtitulo = obtener("#subtituloPagina");
const cajaValidacion = obtener("#mensajeValidacion");
const inputNumero = obtener("#numeroFactura");
const inputFecha = obtener("#fechaEmision");
const selectPuntoVenta = obtener("#puntoVentaId");
const inputCliente = obtener("#buscarCliente");
const inputClienteId = obtener("#clienteId");
const selectCondicionIva = obtener("#condicionIvaId");
const selectListaPrecio = obtener("#listaPrecioId");
const botonQuitarCliente = obtener("#btnQuitarCliente");
const botonAgregar = obtener("#btnAgregarItem");
const botonGuardar = obtener("button[type='submit']", formulario);
const cuerpoDetalles = obtener("#tablaDetalles");
const textoNeto = obtener("#importeNeto");
const textoIva = obtener("#importeIva");
const textoTotal = obtener("#importeTotal");
// -------- Estado en memoria
let catalogo = []; // artículos de la lista de precios ELEGIDA
const items = []; // renglones cargados por el usuario
// -------- Cálculos
function calcularImportes(item) {
    if (item.articulo === null)
        return { neto: 0, iva: 0, subtotal: 0 };
    const bruto = item.cantidad * item.articulo.precioVenta;
    const neto = bruto - bruto * (item.porcentajeBonificacion / 100);
    const iva = neto * ALICUOTA_IVA;
    return { neto, iva, subtotal: neto + iva };
}
function actualizarResumen() {
    let neto = 0;
    let iva = 0;
    for (const item of items) {
        const importes = calcularImportes(item);
        neto += importes.neto;
        iva += importes.iva;
    }
    textoNeto.textContent = formatearMoneda(neto);
    textoIva.textContent = formatearMoneda(iva);
    textoTotal.textContent = formatearMoneda(neto + iva);
}
// ------- Tabla de renglones
function envolverEnCelda(elemento) {
    const celda = document.createElement("td");
    celda.appendChild(elemento);
    return celda;
}
function crearInputNumero(valor, min, step) {
    const input = document.createElement("input");
    input.type = "number";
    input.min = min;
    input.step = step;
    input.value = String(valor);
    return input;
}
function crearFilaItem(item) {
    // --- <select> de artículos (solo los de la lista de precios elegida)
    const selectArticulo = document.createElement("select");
    selectArticulo.innerHTML = `<option value="">Seleccionar artículo</option>`;
    for (const articulo of catalogo) {
        const opcion = document.createElement("option");
        opcion.value = String(articulo.listaPrecioArticuloId);
        opcion.textContent = `${articulo.codigo} — ${articulo.denominacion}`;
        opcion.selected = item.articulo?.listaPrecioArticuloId === articulo.listaPrecioArticuloId;
        selectArticulo.appendChild(opcion);
    }
    const inputCantidad = crearInputNumero(item.cantidad, "0.01", "any");
    const inputBonificacion = crearInputNumero(item.porcentajeBonificacion, "0", "any");
    const celdaPrecio = document.createElement("td");
    const celdaSubtotal = document.createElement("td");
    const botonQuitar = document.createElement("button");
    botonQuitar.type = "button";
    botonQuitar.className = "btn-peligro";
    botonQuitar.textContent = "Quitar";
    // Actualiza SOLO los textos de esta fila + el resumen (sin redibujar la tabla,
    // para no perder el foco mientras el usuario escribe).
    const refrescarFila = () => {
        celdaPrecio.textContent = item.articulo ? formatearMoneda(item.articulo.precioVenta) : "—";
        celdaSubtotal.textContent = formatearMoneda(calcularImportes(item).subtotal);
        actualizarResumen();
    };
    // --- Eventos: cada cambio del usuario se copia al objeto "item" en memoria
    selectArticulo.addEventListener("change", () => {
        item.articulo = catalogo.find((a) => String(a.listaPrecioArticuloId) === selectArticulo.value) ?? null; // find devuelve undefined si no encuentra
        refrescarFila();
    });
    inputCantidad.addEventListener("input", () => {
        item.cantidad = Number(inputCantidad.value);
        refrescarFila();
    });
    inputBonificacion.addEventListener("input", () => {
        item.porcentajeBonificacion = Number(inputBonificacion.value);
        refrescarFila();
    });
    botonQuitar.addEventListener("click", () => {
        items.splice(items.indexOf(item), 1); // sacamos el renglón del array...
        renderizarTabla(); // ...y redibujamos
    });
    refrescarFila(); // valores iniciales
    const fila = document.createElement("tr");
    fila.append(envolverEnCelda(selectArticulo), envolverEnCelda(inputCantidad), celdaPrecio, envolverEnCelda(inputBonificacion), celdaSubtotal, envolverEnCelda(botonQuitar));
    return fila;
}
function renderizarTabla() {
    cuerpoDetalles.innerHTML = "";
    if (items.length === 0) {
        const mensaje = selectListaPrecio.value === ""
            ? "Elegí una lista de precios para poder agregar productos."
            : "Todavía no agregaste productos.";
        cuerpoDetalles.innerHTML = `<tr><td colspan="6" class="sin-datos">${mensaje}</td></tr>`;
    }
    else {
        for (const item of items) {
            cuerpoDetalles.appendChild(crearFilaItem(item));
        }
    }
    actualizarResumen();
}
// -------- Lista de precios
async function cargarListasPrecio() {
    const listas = await servicio.obtenerListasPrecio();
    for (const lista of listas) {
        const opcion = document.createElement("option");
        opcion.value = String(lista.id);
        opcion.textContent = `${lista.codigo} — ${lista.denominacion}`;
        selectListaPrecio.appendChild(opcion);
    }
}
/**
 * Se ejecuta cada vez que el usuario cambia la lista de precios.
 * 1) Pide al backend los artículos de la lista nueva.
 * 2) Los renglones ya cargados se "traducen" a la lista nueva buscando el mismo artículo por código:
 *    si existe, se conserva con el precio nuevo; si no existe, se quita y se avisa.
 */
async function alCambiarLista() {
    const listaId = selectListaPrecio.value;
    botonAgregar.disabled = listaId === "";
    if (listaId === "") { // volvió a "Seleccionar...": no hay lista, no hay artículos
        catalogo = [];
        items.length = 0;
        renderizarTabla();
        return;
    }
    try {
        catalogo = await servicio.obtenerArticulos(Number(listaId));
    }
    catch (error) {
        mostrarErrores([`No se pudo cargar los artículos de la lista: ${mensajeDeError(error)}`]);
        return;
    }
    const cantidadAntes = items.length;
    const conservados = [];
    for (const item of items) {
        if (item.articulo === null) { // renglón todavía sin artículo: se queda tal cual
            conservados.push(item);
            continue;
        }
        const codigo = item.articulo.codigo;
        const equivalente = catalogo.find((a) => a.codigo === codigo);
        if (equivalente !== undefined) {
            item.articulo = equivalente; // mismo artículo, pero con el precio de la lista nueva
            conservados.push(item);
        }
    }
    items.splice(0, items.length, ...conservados); // reemplazamos el contenido del array "items"
    renderizarTabla();
    const quitados = cantidadAntes - items.length;
    if (quitados > 0) {
        mostrarErrores([`Se quitaron ${quitados} renglón(es) porque ese artículo no existe en la lista elegida.`]);
    }
}
// -------- Validación y envío
function validar() {
    const errores = [];
    const datos = new FormData(formulario);
    // En EDICIÓN estos campos (incluida la lista de precios) están deshabilitados: FormData los ignora
    // y además no se pueden cambiar, así que solo los validamos al crear.
    if (!modoEdicion) {
        const numero = Number(leerTexto(datos, "numeroFactura"));
        if (!Number.isInteger(numero) || numero < 1) {
            errores.push("Ingresá un número de factura válido (entero mayor a 0).");
        }
        if (leerTexto(datos, "puntoVentaId") === "")
            errores.push("Seleccioná un punto de venta.");
        if (leerTexto(datos, "condicionIvaId") === "")
            errores.push("Seleccioná la condición IVA.");
        if (leerTexto(datos, "listaPrecioId") === "")
            errores.push("Seleccioná una lista de precios.");
    }
    if (items.length === 0)
        errores.push("Agregá al menos un producto.");
    items.forEach((item, indice) => {
        const n = indice + 1;
        if (item.articulo === null)
            errores.push(`Renglón ${n}: seleccioná un artículo.`);
        if (!(item.cantidad > 0))
            errores.push(`Renglón ${n}: la cantidad debe ser mayor a 0.`);
        if (item.porcentajeBonificacion < 0 || item.porcentajeBonificacion > 100) {
            errores.push(`Renglón ${n}: la bonificación debe estar entre 0 y 100.`);
        }
    });
    return errores;
}
function mostrarErrores(errores) {
    const lista = document.createElement("ul");
    for (const error of errores) {
        const li = document.createElement("li");
        li.textContent = error;
        lista.appendChild(li);
    }
    cajaValidacion.innerHTML = "";
    cajaValidacion.appendChild(lista);
    cajaValidacion.hidden = false;
    cajaValidacion.scrollIntoView({ behavior: "smooth", block: "center" });
}
// Renglones del formulario -> formato que espera el backend (sirve para alta y edición)
function construirDetalles() {
    const detalles = [];
    for (const item of items) {
        if (item.articulo === null)
            continue; // ya validado; esto es para que TS lo sepa (narrowing)
        detalles.push({
            articuloId: item.articulo.listaPrecioArticuloId,
            cantidad: item.cantidad,
            porcentajeBonificacion: item.porcentajeBonificacion
        });
    }
    return detalles;
}
function construirRequestAlta() {
    const datos = new FormData(formulario);
    const clienteId = leerTexto(datos, "clienteId");
    return {
        numero: Number(leerTexto(datos, "numeroFactura")),
        clienteId: clienteId !== "" ? Number(clienteId) : undefined, // sin cliente = Consumidor Final
        condicionIvaId: Number(leerTexto(datos, "condicionIvaId")),
        puntoVentaId: Number(leerTexto(datos, "puntoVentaId")),
        tipoMonedaId: Number(leerTexto(datos, "tipoMonedaId")),
        listaPrecioId: Number(leerTexto(datos, "listaPrecioId")),
        importeCobrado: 0,
        detalles: construirDetalles()
    };
}
// En edición mandamos SOLO los renglones. Lo que no mandamos, el backend lo deja como estaba (PUT parcial).
// La lista de precios no se manda: el backend la deduce de los renglones que ya tiene la factura.
function construirRequestEdicion() {
    return { detalles: construirDetalles() };
}
async function guardarFactura() {
    const errores = validar();
    if (errores.length > 0) {
        mostrarErrores(errores);
        return;
    }
    cajaValidacion.hidden = true;
    botonGuardar.disabled = true;
    botonGuardar.textContent = "Guardando...";
    try {
        if (facturaId !== null) {
            const actualizada = await servicio.actualizarFactura(facturaId, construirRequestEdicion());
            window.location.href = `index.html?editada=${actualizada.numeroFactura}`;
        }
        else {
            const creada = await servicio.crearFactura(construirRequestAlta());
            // Volvemos al listado; index.html lee ?creada=... y muestra el mensaje de éxito.
            window.location.href = `index.html?creada=${creada.numeroFactura}`;
        }
    }
    catch (error) {
        mostrarErrores([mensajeDeError(error)]);
        botonGuardar.disabled = false;
        botonGuardar.textContent = textoBotonGuardar;
    }
}
// --------- Cliente
async function abrirSelectorCliente() {
    if (modoEdicion)
        return; // en edición el cliente no se puede cambiar
    const cliente = await selectorCliente.elegir();
    if (cliente === null)
        return;
    inputCliente.value = cliente.denominacion;
    inputClienteId.value = String(cliente.id);
    botonQuitarCliente.hidden = false;
}
inputCliente.addEventListener("click", () => void abrirSelectorCliente());
inputCliente.addEventListener("keydown", (event) => {
    if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        void abrirSelectorCliente();
    }
});
botonQuitarCliente.addEventListener("click", () => {
    inputCliente.value = "";
    inputClienteId.value = "";
    botonQuitarCliente.hidden = true;
});
// ------ Eventos generales
selectListaPrecio.addEventListener("change", () => void alCambiarLista());
botonAgregar.addEventListener("click", () => {
    if (selectListaPrecio.value === "")
        return; // sin lista no hay artículos para elegir
    items.push({ articulo: null, cantidad: 1, porcentajeBonificacion: 0 });
    renderizarTabla();
});
formulario.addEventListener("submit", (event) => {
    event.preventDefault();
    void guardarFactura();
});
// -------- Modo edición: precargar datos y deshabilitar campos
/** Selecciona la opción con ese id; si el <select> no la tiene (opciones fijas del HTML), la crea. */
function fijarSelect(select, id, texto) {
    const valor = String(id);
    const existe = Array.from(select.options).some((opcion) => opcion.value === valor);
    if (!existe) {
        const opcion = document.createElement("option");
        opcion.value = valor;
        opcion.textContent = texto;
        select.appendChild(opcion);
    }
    select.value = valor;
}
async function cargarFacturaParaEditar(id) {
    const factura = await servicio.obtenerFacturaParaEdicion(id);
    if (factura.estado === "ANULADA") {
        throw new Error("La factura está anulada y no se puede editar.");
    }
    // La lista se deduce de los renglones: sin renglones no sabemos de qué lista son los artículos.
    if (factura.listaPrecioId === null) {
        throw new Error("Esta factura no tiene artículos, por lo que no se puede determinar su lista de precios.");
    }
    // Textos de la pantalla
    document.title = `Editar Factura | Sistema de Facturación`;
    titulo.textContent = `Editar Factura N° ${factura.numero}`;
    subtitulo.textContent = "Podés agregar o quitar productos de la factura. La lista de precios no se puede cambiar.";
    // Campos que solo se MUESTRAN: los cargamos y los deshabilitamos
    inputNumero.value = String(factura.numero);
    inputFecha.value = factura.fechaEmision.substring(0, 10); // "2026-10-31T00:00..." -> "2026-10-31"
    fijarSelect(selectPuntoVenta, factura.puntoVentaId, factura.puntoVentaDescripcion);
    fijarSelect(selectCondicionIva, factura.condicionIvaId, factura.condicionIvaDenominacion);
    inputCliente.value = factura.clienteDenominacion;
    inputClienteId.value = factura.clienteId !== null ? String(factura.clienteId) : "";
    inputNumero.disabled = true;
    inputFecha.disabled = true;
    selectPuntoVenta.disabled = true;
    selectCondicionIva.disabled = true;
    inputCliente.disabled = true;
    botonQuitarCliente.hidden = true;
    // Lista de precios: se muestra deshabilitada y se carga el catálogo de ESA lista
    fijarSelect(selectListaPrecio, factura.listaPrecioId, factura.listaPrecioDescripcion ?? "Lista de precios");
    selectListaPrecio.disabled = true;
    catalogo = await servicio.obtenerArticulos(factura.listaPrecioId);
    // Renglones que ya tenía la factura
    for (const detalle of factura.detalles) {
        // Normalmente el artículo está en el catálogo. Si no (p. ej. lo dieron de baja), lo armamos
        // con los datos del renglón guardado para no perderlo.
        const articulo = catalogo.find((a) => a.listaPrecioArticuloId === detalle.listaPrecioArticuloId) ?? {
            listaPrecioArticuloId: detalle.listaPrecioArticuloId,
            codigo: detalle.codigo,
            denominacion: detalle.denominacion,
            precioVenta: detalle.precioUnitario
        };
        if (!catalogo.includes(articulo))
            catalogo.push(articulo);
        items.push({
            articulo,
            cantidad: detalle.cantidad,
            porcentajeBonificacion: detalle.porcentajeBonificacion
        });
    }
}
// -------- Arranque
async function iniciar() {
    botonGuardar.textContent = textoBotonGuardar;
    botonAgregar.disabled = true;
    inputFecha.value = fechaDeHoy(); // en alta: la fecha real la fija el servidor; acá solo se muestra
    try {
        await cargarListasPrecio(); // primero las opciones del <select>...
        if (facturaId !== null) {
            await cargarFacturaParaEditar(facturaId); // ...después seleccionamos la de la factura
        }
    }
    catch (error) {
        mostrarErrores([mensajeDeError(error)]);
        botonGuardar.disabled = true; // si no se pudo cargar bien, no dejamos guardar
    }
    botonAgregar.disabled = selectListaPrecio.value === "";
    renderizarTabla();
}
void iniciar();
