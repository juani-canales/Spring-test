package com.facturacion.api.facturas;


import com.facturacion.DTO.FacturaReporteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class FacturaRestController {

    @GetMapping
    public Map<String, Object> listar (@RequestParam(required = false) Date fechaDesde,
                                       @RequestParam(required = false) Date fechaHasta,
                                       @RequestParam(required = false) String estado,
                                       @RequestParam(required = false) Double montoMinimo) {

        return null;
    }

}
