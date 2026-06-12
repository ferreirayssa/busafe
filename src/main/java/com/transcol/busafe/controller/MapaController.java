package com.transcol.busafe.controller;

import com.transcol.busafe.service.MapaOSRMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mapa")
public class MapaController {

    @Autowired
    private MapaOSRMService mapaOSRMService;

    /**
     * Calcular rota entre dois pontos usando OSRM
     * GET /api/mapa/rota-osrm?origemLat=-20.3155&origemLng=-40.3128&destinoLat=-20.2781&destinoLng=-40.2945
     */
    @GetMapping("/rota-osrm")
    public ResponseEntity<?> calcularRotaOSRM(
            @RequestParam double origemLat,
            @RequestParam double origemLng,
            @RequestParam double destinoLat,
            @RequestParam double destinoLng) {
        
        Map<String, Object> resultado = mapaOSRMService.calcularRota(origemLat, origemLng, destinoLat, destinoLng);
        return ResponseEntity.ok(resultado);
    }
}