// Funciones de ayuda compartidas por todas las pantallas.
/**
 * Busca un elemento en el DOM y, si NO existe, lanza un error claro.
 * Así nos ahorramos escribir "if (elemento)" en cada selección (null safety, Clase 3).
 * <T extends HTMLElement> significa: "T es algún tipo de elemento HTML".
 */
export function obtener(selector, raiz = document) {
    const elemento = raiz.querySelector(selector);
    if (elemento === null) {
        throw new Error(`No se encontró el elemento "${selector}" en el HTML`);
    }
    return elemento;
}
/** Lee un campo de un FormData como texto (typeof como type guard, Clase 4). */
export function leerTexto(datos, nombre) {
    const valor = datos.get(nombre); // string | File | null
    return typeof valor === "string" ? valor.trim() : "";
}
/** Convierte cualquier error capturado en un mensaje legible para el usuario. */
export function mensajeDeError(error) {
    if (error instanceof TypeError) {
        // fetch lanza TypeError cuando no puede conectarse (servidor caído, CORS, sin red)
        return "No se pudo conectar con el servidor. ¿Está levantado el backend en el puerto 8080?";
    }
    if (error instanceof Error) {
        return error.message;
    }
    return "Ocurrió un error inesperado.";
}
/**
 * fetch NO falla cuando el servidor responde 404 o 500: hay que revisar response.ok (Clase 5).
 * Si la respuesta no es exitosa, intenta leer el {"mensaje": "..."} que manda el backend.
 */
export async function verificarRespuesta(respuesta) {
    if (respuesta.ok)
        return;
    let mensaje = `Error en la petición (HTTP ${respuesta.status})`;
    try {
        const cuerpo = await respuesta.json();
        if (cuerpo.mensaje)
            mensaje = cuerpo.mensaje;
    }
    catch {
        // el cuerpo no era JSON: nos quedamos con el mensaje genérico
    }
    throw new Error(mensaje);
}
/** "2026-10-31T00:00:00.000+00:00" -> "31/10/2026". No usamos new Date() para evitar corrimientos por zona horaria. */
export function formatearFecha(fechaIso) {
    const [anio, mes, dia] = fechaIso.substring(0, 10).split("-");
    return `${dia}/${mes}/${anio}`;
}
const formatoMoneda = new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS" });
export function formatearMoneda(importe) {
    return formatoMoneda.format(importe);
}
/** Fecha de hoy en formato "yyyy-MM-dd" (el que usa <input type="date">), en hora local. */
export function fechaDeHoy() {
    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, "0");
    const dia = String(hoy.getDate()).padStart(2, "0");
    return `${hoy.getFullYear()}-${mes}-${dia}`;
}
/** Crea un <td> usando textContent (seguro: nunca interpreta el texto como HTML). */
export function crearCelda(texto, clase) {
    const celda = document.createElement("td");
    celda.textContent = texto;
    if (clase)
        celda.className = clase;
    return celda;
}
/**
 * Dibuja los botones "Anterior / Siguiente" + "Página X de Y".
 * Se reutiliza para facturas y para clientes. paginaActual es 0-based.
 * alCambiar recibe el número (0-based) de la página a la que hay que ir.
 */
export function renderizarPaginacion(contenedor, paginaActual, totalPaginas, totalElementos, alCambiar) {
    contenedor.innerHTML = "";
    if (totalElementos === 0)
        return;
    const crearBoton = (texto, deshabilitado, destino) => {
        const boton = document.createElement("button");
        boton.type = "button"; // type="button": que no envíe ningún formulario
        boton.className = "btn-secundario";
        boton.textContent = texto;
        boton.disabled = deshabilitado;
        boton.addEventListener("click", () => alCambiar(destino));
        return boton;
    };
    const info = document.createElement("span");
    info.className = "paginacion-info";
    info.textContent = `Página ${paginaActual + 1} de ${totalPaginas} · ${totalElementos} resultado(s)`;
    contenedor.append(crearBoton("← Anterior", paginaActual <= 0, paginaActual - 1), info, crearBoton("Siguiente →", paginaActual >= totalPaginas - 1, paginaActual + 1));
}
