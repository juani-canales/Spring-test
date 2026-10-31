import { ClienteService } from "./ClienteService.js";
import type { ClienteFiltrosDTO, ClienteResumenDTO, Pagina } from "./models/factura.dto.js";
import { crearCelda, leerTexto, mensajeDeError, obtener, renderizarPaginacion } from "./utils.js";

const TAMANIO_PAGINA = 10;

/**
 * Ventana (popover) para buscar y elegir un cliente, con filtros y paginación.
 *
 * Uso:
 *     const selector = new SelectorCliente();
 *     const cliente = await selector.elegir();   // espera a que el usuario elija o cancele
 *     if (cliente !== null) { ... }
 *
 * Usa el elemento nativo <dialog>: el navegador se encarga del fondo oscuro,
 * de cerrar con Escape y de que el foco quede dentro de la ventana.
 */
export class SelectorCliente {
    private readonly servicio = new ClienteService();

    private readonly dialogo: HTMLDialogElement;
    private readonly formulario: HTMLFormElement;
    private readonly cuerpoTabla: HTMLTableSectionElement;
    private readonly contenedorPaginacion: HTMLElement;
    private readonly textoEstado: HTMLElement;

    private filtros: ClienteFiltrosDTO = {};

    // Función que "despierta" a quien hizo await selector.elegir()
    private resolver: ((cliente: ClienteResumenDTO | null) => void) | null = null;

    constructor() {
        // El HTML del popover vive acá, así lo reutilizan las dos pantallas sin duplicarlo.
        // (Es texto fijo; los datos de los clientes se insertan después con textContent.)
        this.dialogo = document.createElement("dialog");
        this.dialogo.className = "dialogo-clientes";
        this.dialogo.innerHTML = `
            <div class="dialogo-cabecera">
                <h3>Buscar cliente</h3>
                <button type="button" class="dialogo-cerrar" aria-label="Cerrar">✕</button>
            </div>

            <form class="dialogo-filtros">
                <div class="grupo-filtros">
                    <div class="campo">
                        <label for="sc-denominacion">Denominación</label>
                        <input type="text" id="sc-denominacion" name="denominacion" placeholder="Nombre o razón social">
                    </div>
                    <div class="campo">
                        <label for="sc-cuit">CUIT / CUIL</label>
                        <input type="text" id="sc-cuit" name="cuitCuil" placeholder="Ej.: 20-12345678-9">
                    </div>
                    <div class="campo">
                        <label for="sc-contacto">Contacto</label>
                        <input type="text" id="sc-contacto" name="contacto" placeholder="Email, teléfono o celular">
                    </div>
                    <div class="campo">
                        <label for="sc-domicilio">Domicilio</label>
                        <input type="text" id="sc-domicilio" name="domicilio" placeholder="Calle">
                    </div>
                </div>
                <div class="acciones-filtros">
                    <button type="button" class="btn-secundario sc-limpiar">Limpiar</button>
                    <button type="submit" class="btn-primario">Buscar</button>
                </div>
            </form>

            <p class="dialogo-estado" role="status"></p>

            <div class="tabla-contenedor">
                <table>
                    <thead>
                        <tr>
                            <th>Denominación</th><th>CUIT / CUIL</th><th>Contacto</th><th>Domicilio</th><th></th>
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

    /** Abre el popover y devuelve una Promesa que se resuelve con el cliente elegido, o null si cancela. */
    public elegir(): Promise<ClienteResumenDTO | null> {
        this.limpiar();               // siempre arranca sin filtros y muestra la página 1
        this.dialogo.showModal();     // abre la ventana por encima del resto de la página

        return new Promise<ClienteResumenDTO | null>((resolve) => {
            this.resolver = resolve;  // guardamos "resolve" para llamarlo cuando el usuario decida
        });
    }

    // ------------------------------------------------------------------ eventos
    private conectarEventos(): void {
        this.formulario.addEventListener("submit", (event: Event) => {
            event.preventDefault();
            const datos = new FormData(this.formulario);
            this.filtros = {
                denominacion: leerTexto(datos, "denominacion"),
                cuitCuil: leerTexto(datos, "cuitCuil"),
                contacto: leerTexto(datos, "contacto"),
                domicilio: leerTexto(datos, "domicilio")
            };
            void this.cargar(0);      // una búsqueda nueva siempre vuelve a la página 1
        });

        obtener<HTMLButtonElement>(".sc-limpiar", this.dialogo)
            .addEventListener("click", () => this.limpiar());

        obtener<HTMLButtonElement>(".dialogo-cerrar", this.dialogo)
            .addEventListener("click", () => this.dialogo.close());

        // 'close' se dispara SIEMPRE que la ventana se cierra (✕, tecla Escape o después de elegir).
        // Si nadie eligió a nadie, resolvemos con null.
        this.dialogo.addEventListener("close", () => this.finalizar(null));
    }

    private limpiar(): void {
        this.formulario.reset();
        this.filtros = {};
        void this.cargar(0);
    }

    private finalizar(cliente: ClienteResumenDTO | null): void {
        const resolver = this.resolver;
        this.resolver = null;         // lo anulamos: una Promesa solo se puede resolver una vez
        resolver?.(cliente);
    }

    private elegirCliente(cliente: ClienteResumenDTO): void {
        this.finalizar(cliente);      // primero entregamos el cliente...
        this.dialogo.close();         // ...y después cerramos (el evento 'close' ya no tiene a quién avisar)
    }

    // ------------------------------------------------------------ datos y dibujo
    private async cargar(pagina: number): Promise<void> {
        this.textoEstado.textContent = "Cargando clientes...";
        try {
            const resultado = await this.servicio.buscarClientes(this.filtros, pagina, TAMANIO_PAGINA);
            this.dibujar(resultado);
        } catch (error) {
            this.cuerpoTabla.innerHTML = "";
            this.contenedorPaginacion.innerHTML = "";
            this.textoEstado.textContent = mensajeDeError(error);
        }
    }

    private dibujar(resultado: Pagina<ClienteResumenDTO>): void {
        this.cuerpoTabla.innerHTML = "";
        this.textoEstado.textContent =
            resultado.totalElementos === 0 ? "No se encontraron clientes con esos criterios." : "";

        for (const cliente of resultado.contenido) {
            const contacto = [cliente.email, cliente.telefono].filter(Boolean).join(" · ");
            const domicilio = [cliente.nombreCalle, cliente.numeroCalle].filter(Boolean).join(" ");

            const botonElegir = document.createElement("button");
            botonElegir.type = "button";
            botonElegir.className = "btn-primario";
            botonElegir.textContent = "Elegir";
            botonElegir.addEventListener("click", () => this.elegirCliente(cliente));

            const celdaBoton = document.createElement("td");
            celdaBoton.appendChild(botonElegir);

            const fila = document.createElement("tr");
            fila.append(
                crearCelda(cliente.denominacion),
                crearCelda(cliente.cuitCuil),
                crearCelda(contacto || "—"),
                crearCelda(domicilio || "—"),
                celdaBoton
            );
            this.cuerpoTabla.appendChild(fila);
        }

        // Al hacer clic en "Siguiente" pedimos esa página al backend (de 10 en 10).
        renderizarPaginacion(
            this.contenedorPaginacion,
            resultado.pagina,
            resultado.totalPaginas,
            resultado.totalElementos,
            (nuevaPagina) => void this.cargar(nuevaPagina)
        );
    }
}
