package com.facturacion.controller;

import com.facturacion.DTO.FacturaReporteDTO;
import com.facturacion.DTO.FacturaRequestDTO;
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

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FacturaRestController {

    private final FacturaService service;

    @GetMapping
    public List<FacturaReporteDTO> listar (@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaDesde,
                                           @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fechaHasta,
                                           @RequestParam(required = false) String estado,
                                           @RequestParam(required = false) Double montoMinimo) {

        return service.buscarFacturasFiltradas(fechaDesde, fechaHasta, estado, montoMinimo);
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

    // Un único lugar que convierte "no existe" en 404 con un mensaje legible para el frontend. Recomendado por IA
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> manejarNoEncontrado(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
    }
}
