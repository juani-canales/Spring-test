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
        try {
            FacturaReporteDTO facturaCreada = service.crear(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(facturaCreada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaReporteDTO> actualizar(@PathVariable Long id,
                                                        @RequestBody FacturaRequestDTO dto) {
        try {
            FacturaReporteDTO facturaActualizada = service.actualizar(id, dto);
            return ResponseEntity.ok(facturaActualizada);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
