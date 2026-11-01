package com.facturacion.controller;

import com.facturacion.DTO.ClienteResumenDTO;
import com.facturacion.DTO.PaginaDTO;
import com.facturacion.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteRestController {

    private final ClienteService service;

    // GET /api/clientes?denominacion=juan&pagina=0&tamanio=5
    @GetMapping
    public PaginaDTO<ClienteResumenDTO> buscar(
            @RequestParam(required = false) String denominacion,
            @RequestParam(required = false) String cuitCuil,
            @RequestParam(required = false) String contacto,
            @RequestParam(required = false) String domicilio,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int tamanio) {

        return service.buscar(denominacion, cuitCuil, contacto, domicilio, pagina, tamanio);
    }
}
