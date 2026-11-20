import { ArticuloService } from "./ArticuloService.js";
import type { ArticuloFiltrosDTO, ArticuloPrecioDTO, Pagina } from "./models/factura.dto.js";
import { crearCelda, formatearMoneda, leerTexto, mensajeDeError, obtener, renderizarPaginacion } from "./utils.js";

const TAMANIO_PAGINA = 5;

/**
 * Ventana (popover) para buscar y elegir un artículo DE UNA LISTA DE PRECIOS, con filtros y paginación.
 *
 * Uso:
 *     const selector = new SelectorArticulo();
 *     const articulo = await selector.elegir(listaPrecioId);   // espera a que el usuario elija o cancele
 *     if (articulo !== null) { ... }
 */
export class SelectorArticulo {
    private readonly servicio = new ArticuloService();

    private readonly dialogo: HTMLDialogElement;
    private readonly formulario: HTMLFormElement;
    private readonly cuerpoTabla: HTMLTableSectionElement;
    private readonly contenedorPaginacion: HTMLElement;
    private readonly textoEstado: HTMLElement;

    private filtros: ArticuloFiltrosDTO = {};
    private listaPrecioId = 0;        // la lista cuyos artículos se muestran (la fija elegir())

    // Función que "despierta" a quien hizo await selector.elegir(...)
    private resolver: ((articulo: ArticuloPrecioDTO | null) => void) | null = null;

    constructor() {
        // Reutilizamos las clases de estilo del popover de clientes: mismo aspecto sin duplicar CSS.
        // (Es texto fijo; los datos de los artículos se insertan después con textContent.)
        this.dialogo = document.createElement("dialog");
        this.dialogo.className = "dialogo-clientes dialogo-articulos";
        this.dialogo.innerHTML = `
            <div class="dialogo-cabecera">
                <h3>Buscar artículo</h3>
                <button type="button" class="dialogo-cerrar" aria-label="Cerrar">✕</button>
            </div>

            <form class="dialogo-filtros">
                <div class="grupo-filtros">
                    <div class="campo">
                        <label for="sa-codigo">Código</label>
                        <input type="text" id="sa-codigo" name="codigo" placeholder="Código del artículo">
                    </div>
                    <div class="campo">
                        <label for="sa-denominacion">Denominación</label>
                        <input type="text" id="sa-denominacion" name="denominacion" placeholder="Nombre del artículo">
                    </div>
                    <div class="campo">
                        <label for="sa-marca">Marca</label>
                        <input type="text" id="sa-marca" name="marca" placeholder="Ej.: Logitech">
                    </div>
                    <div class="campo">
                        <label for="sa-rubro">Rubro</label>
                        <input type="text" id="sa-rubro" name="rubro" placeholder="Ej.: Computación">
                    </div>
                    <div class="campo">
                        <label for="sa-precio">Precio mínimo ($)</label>
                        <input type="number" id="sa-precio" name="precioMinimo" min="0" step="any"
                               placeholder="Ej.: 5000 (ese precio o más)">
                    </div>
                </div>
                <div class="acciones-filtros">
                    <button type="button" class="btn-secundario sa-limpiar">Limpiar</button>
                    <button type="submit" class="btn-primario">Buscar</button>
                </div>
            </form>

            <p class="dialogo-estado" role="status"></p>

            <div class="tabla-contenedor">
                <table>
                    <thead>
                        <tr>
                            <th>Código</th><th>Denominación</th><th>Marca</th><th>Rubro</th><th>Precio</th><th></th>
                        </tr>
                    </thead>
                    <tbody></tbody>
                </table>
            </div>

            <div class="paginacion"></div>
        `;
        document.body.appendChild(this.dialogo);

        // Buscamos las piezas internas UNA vez y las guardamos.
        this.formulario = obtener<HTMLFormElement>("form", this.dialogo);
        this.cuerpoTabla = obtener<HTMLTableSectionElement>("tbody", this.dialogo);
        this.contenedorPaginacion = obtener<HTMLElement>(".paginacion", this.dialogo);
        this.textoEstado = obtener<HTMLElement>(".dialogo-estado", this.dialogo);

        this.conectarEventos();
    }

    /** Abre el popover con los artículos de ESA lista; devuelve el artículo elegido, o null si cancela. */
    public elegir(listaPrecioId: number): Promise<ArticuloPrecioDTO | null> {
        this.listaPrecioId = listaPrecioId;   // primero la lista: limpiar() ya hace una búsqueda con ella
        this.limpiar();                       // siempre arranca sin filtros y muestra la página 1
        this.dialogo.showModal();

        return new Promise<ArticuloPrecioDTO | null>((resolve) => {
            this.resolver = resolve;
        });
    }

    // --------- eventos
    private conectarEventos(): void {
        this.formulario.addEventListener("submit", (event: Event) => {
            event.preventDefault();
            const datos = new FormData(this.formulario);

            // El input numérico devuelve "" si está vacío; en ese caso no hay filtro de precio.
            const precio = leerTexto(datos, "precioMinimo");

            this.filtros = {
                codigo: leerTexto(datos, "codigo"),
                denominacion: leerTexto(datos, "denominacion"),
                marca: leerTexto(datos, "marca"),
                rubro: leerTexto(datos, "rubro"),
                precioMinimo: precio !== "" ? Number(precio) : undefined
            };
            void this.cargar(0);              // una búsqueda nueva siempre vuelve a la página 1
        });

        obtener<HTMLButtonElement>(".sa-limpiar", this.dialogo)
            .addEventListener("click", () => this.limpiar());

        obtener<HTMLButtonElement>(".dialogo-cerrar", this.dialogo)
            .addEventListener("click", () => this.dialogo.close());

        // 'close' se dispara SIEMPRE que la ventana se cierra (✕, tecla Escape o después de elegir).
        // Si nadie eligió nada, resolvemos con null.
        this.dialogo.addEventListener("close", () => this.finalizar(null));
    }

    private limpiar(): void {
        this.formulario.reset();
        this.filtros = {};
        void this.cargar(0);
    }

    private finalizar(articulo: ArticuloPrecioDTO | null): void {
        const resolver = this.resolver;
        this.resolver = null;                 // una Promesa solo se puede resolver una vez
        resolver?.(articulo);
    }

    private elegirArticulo(articulo: ArticuloPrecioDTO): void {
        this.finalizar(articulo);             // primero entregamos el artículo...
        this.dialogo.close();                 // ...y después cerramos (el evento 'close' ya no tiene a quién avisar)
    }

    // ------- datos y dibujo
    private async cargar(pagina: number): Promise<void> {
        this.textoEstado.textContent = "Cargando artículos...";
        try {
            const resultado = await this.servicio.buscarArticulos(
                this.listaPrecioId, this.filtros, pagina, TAMANIO_PAGINA);
            this.dibujar(resultado);
        } catch (error) {
            this.cuerpoTabla.innerHTML = "";
            this.contenedorPaginacion.innerHTML = "";
            this.textoEstado.textContent = mensajeDeError(error);
        }
    }

    private dibujar(resultado: Pagina<ArticuloPrecioDTO>): void {
        this.cuerpoTabla.innerHTML = "";
        this.textoEstado.textContent = resultado.totalElementos === 0
            ? "No se encontraron artículos con esos criterios en la lista de precios elegida."
            : "";

        for (const articulo of resultado.contenido) {
            const botonElegir = document.createElement("button");
            botonElegir.type = "button";
            botonElegir.className = "btn-primario";
            botonElegir.textContent = "Elegir";
            botonElegir.addEventListener("click", () => this.elegirArticulo(articulo));

            const celdaBoton = document.createElement("td");
            celdaBoton.appendChild(botonElegir);

            const fila = document.createElement("tr");
            fila.append(
                crearCelda(articulo.codigo),
                crearCelda(articulo.denominacion),
                crearCelda(articulo.marca ?? "—"),      // ?? = "si es null/undefined, usá esto"
                crearCelda(articulo.rubro ?? "—"),
                crearCelda(formatearMoneda(articulo.precioVenta)),
                celdaBoton
            );
            this.cuerpoTabla.appendChild(fila);
        }

        renderizarPaginacion(
            this.contenedorPaginacion,
            resultado.pagina,
            resultado.totalPaginas,
            resultado.totalElementos,
            (nuevaPagina) => void this.cargar(nuevaPagina)
        );
    }
}