package com.facturacion.controller;

import com.facturacion.DTO.*;
import com.facturacion.service.FacturaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaRestController {

    private final FacturaService service;

    @GetMapping
    public PaginaDTO<FacturaReporteDTO> listar (@RequestParam(required = false) Long numero,
                                                @RequestParam(required = false) Long clienteId,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
                                                @RequestParam(required = false) String estado,
                                                @RequestParam(required = false) Double montoMinimo,
                                                @RequestParam(defaultValue = "0") int pagina,
                                                @RequestParam(defaultValue = "5") int tamanio){

        return service.buscarFacturasFiltradas(numero, clienteId, fechaDesde, fechaHasta, estado, montoMinimo, pagina, tamanio);
    }

    // NUEVO: GET /api/facturas/listas-precio -> para llenar el <select> de lista de precios
    @GetMapping("/listas-precio")
    public List<ListaPrecioDTO> listasPrecio() {
        return service.listarListasPrecio();
    }

    // GET /api/facturas/articulos -> catálogo para el formulario de alta
    @GetMapping("/articulos")
    public List<ArticuloPrecioDTO> articulos(@RequestParam Long listaPrecioId) {
        return service.listarArticulosConPrecio(listaPrecioId);
    }

    // GET /api/facturas/{id} -> datos completos para precargar la pantalla de edición
    // (Spring prefiere las rutas "literales" como /articulos antes que /{id}, no hay conflicto)
    @GetMapping("/{id}")
    public FacturaEdicionDTO obtener(@PathVariable Long id) {
        return service.obtenerParaEdicion(id);
    }

    @PostMapping
    public ResponseEntity<FacturaReporteDTO> crear (@RequestBody FacturaRequestDTO dto) {
        FacturaReporteDTO facturaCreada = service.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(facturaCreada);//201
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaReporteDTO> actualizar(@PathVariable Long id,
                                                        @RequestBody FacturaRequestDTO dto) {
        FacturaReporteDTO facturaActualizada = service.actualizar(id, dto);
        return ResponseEntity.ok(facturaActualizada);//200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();//204
    }

    // Un único lugar que convierte "no existe" en 404 con un mensaje legible para el frontend.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
    }

    // Reglas de negocio incumplidas (artículo de otra lista, factura anulada...) -> 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarReglaDeNegocio(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("mensaje", e.getMessage()));
    }
}
