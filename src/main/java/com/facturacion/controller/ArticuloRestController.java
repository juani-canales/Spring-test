package com.facturacion.controller;

import com.facturacion.DTO.ArticuloPrecioDTO;
import com.facturacion.DTO.PaginaDTO;
import com.facturacion.service.ArticuloService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/articulos")
@RequiredArgsConstructor
public class ArticuloRestController {

    private final ArticuloService service;

    // GET /api/articulos?listaPrecioId=1&marca=log&precioMinimo=5000&pagina=0&tamanio=5
    @GetMapping
    public PaginaDTO<ArticuloPrecioDTO> buscar(
            @RequestParam Long listaPrecioId,                       // obligatorio: los artículos son de UNA lista
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String denominacion,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String rubro,
            @RequestParam(required = false) Double precioMinimo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio) {

        return service.buscar(listaPrecioId, codigo, denominacion, marca, rubro, precioMinimo, pagina, tamanio);
    }
}