import {FacturaFiltrosDTO, FacturaReporteDTO, FacturaRequestDTO} from "./models/factura.dto";

const API = "http://localhost:8080/api/facturas"


export class FacturaService {

    async obtenerFacturas(filtros: FacturaFiltrosDTO): Promise<FacturaReporteDTO> {

        //¿Cómo hago para obtener los filtros que ingreso el suario por el form de filtrado
        // y luego buscar los filtros en el backend?


        const respuesta = await fetch(API);
        if (!respuesta.ok) throw Error(`HTTP ${respuesta.status}`);



        const datos = await respuesta.json();
        return datos.results;
    }


    async crearFactura(factura: FacturaRequestDTO): Promise<FacturaReporteDTO> {

        //Acá de los datos que ingresa del formulario para crear una nueva factura
        // ¿Cómo hago para que el backend cree la nueva factura?


        const respuesta = await fetch(API);
        if (!respuesta.ok) throw Error(`HTTP ${respuesta.status}`);

        const datos = await respuesta.json();
        return datos.results;
    }


    async actualizarEstado(id: number, estado: string, observaciones?: string): Promise<FacturaReporteDTO> {

        //Misma duda que los anteriores métodos.

        const respuesta = await fetch(API);
        if (!respuesta.ok) throw Error(`HTTP ${respuesta.status}`);

        const datos = await respuesta.json();
        return datos.results;
    }


    async eliminarFactura(id: number): Promise<void> {
        //Misma duda que los anteriores métodos
    }


}

