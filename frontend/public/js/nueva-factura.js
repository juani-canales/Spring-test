// Lógica de nueva-factura.html: renglones dinámicos, cálculo de importes y alta.
import { FacturaService } from "./FacturaService.js";
import { SelectorCliente } from "./SelectorCliente.js";
import { fechaDeHoy, formatearMoneda, leerTexto, mensajeDeError, obtener } from "./utils.js";
const ALICUOTA_IVA = 0.21; // el backend usa el mismo 21%: acá es solo una vista previa
// ---------------------------------------------------------------- Elementos DOM
const servicio = new FacturaService();
const selectorCliente = new SelectorCliente();
const formulario = obtener("#formNuevaFactura");
const cajaValidacion = obtener("#mensajeValidacion");
const inputFecha = obtener("#fechaEmision");
const inputCliente = obtener("#buscarCliente");
const inputClienteId = obtener("#clienteId");
const botonQuitarCliente = obtener("#btnQuitarCliente");
const botonAgregar = obtener("#btnAgregarItem");
const botonGuardar = obtener("button[type='submit']", formulario);
const cuerpoDetalles = obtener("#tablaDetalles");
const textoNeto = obtener("#importeNeto");
const textoIva = obtener("#importeIva");
const textoTotal = obtener("#importeTotal");
// --------------------------------------------------------------- Estado en memoria
let catalogo = []; // artículos que trae el backend
const items = []; // renglones cargados por el usuario
// --------------------------------------------------------------------- Cálculos
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
// ------------------------------------------------------------ Tabla de renglones
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
    // --- <select> de artículos
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
        cuerpoDetalles.innerHTML = `<tr><td colspan="6" class="sin-datos">Todavía no agregaste productos.</td></tr>`;
    }
    else {
        for (const item of items) {
            cuerpoDetalles.appendChild(crearFilaItem(item));
        }
    }
    actualizarResumen();
}
// ------------------------------------------------------------- Validación y envío
function validar() {
    const errores = [];
    const datos = new FormData(formulario);
    const numero = Number(leerTexto(datos, "numeroFactura"));
    if (!Number.isInteger(numero) || numero < 1) {
        errores.push("Ingresá un número de factura válido (entero mayor a 0).");
    }
    if (leerTexto(datos, "puntoVentaId") === "")
        errores.push("Seleccioná un punto de venta.");
    if (leerTexto(datos, "condicionIvaId") === "")
        errores.push("Seleccioná la condición IVA.");
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
function construirRequest() {
    const datos = new FormData(formulario);
    const clienteId = leerTexto(datos, "clienteId");
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
    return {
        numero: Number(leerTexto(datos, "numeroFactura")),
        clienteId: clienteId !== "" ? Number(clienteId) : undefined, // sin cliente = Consumidor Final
        condicionIvaId: Number(leerTexto(datos, "condicionIvaId")),
        puntoVentaId: Number(leerTexto(datos, "puntoVentaId")),
        tipoMonedaId: Number(leerTexto(datos, "tipoMonedaId")),
        importeCobrado: 0,
        detalles
    };
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
        const creada = await servicio.crearFactura(construirRequest());
        // Volvemos al listado; index.html lee ?creada=... y muestra el mensaje de éxito.
        window.location.href = `index.html?creada=${creada.numeroFactura}`;
    }
    catch (error) {
        mostrarErrores([mensajeDeError(error)]);
        botonGuardar.disabled = false;
        botonGuardar.textContent = "Guardar factura";
    }
}
// --------- Cliente
async function abrirSelectorCliente() {
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
// ------------------------------------------------------------ Eventos generales
botonAgregar.addEventListener("click", () => {
    items.push({ articulo: null, cantidad: 1, porcentajeBonificacion: 0 });
    renderizarTabla();
});
formulario.addEventListener("submit", (event) => {
    event.preventDefault();
    void guardarFactura();
});
// --------------------------------------------------------------------- Arranque
async function iniciar() {
    inputFecha.value = fechaDeHoy(); // la fecha real la fija el servidor; acá solo se muestra
    try {
        catalogo = await servicio.obtenerArticulos();
    }
    catch (error) {
        mostrarErrores([`No se pudo cargar el catálogo de artículos: ${mensajeDeError(error)}`]);
    }
    renderizarTabla();
}
void iniciar();
