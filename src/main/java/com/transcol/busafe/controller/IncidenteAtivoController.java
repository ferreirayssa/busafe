package com.transcol.busafe.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.transcol.busafe.service.IncidenteAtivoService;

@RestController
@RequestMapping("/api/incidentes-ativos")
public class IncidenteAtivoController {

    private final IncidenteAtivoService service;

    public IncidenteAtivoController(IncidenteAtivoService service) {
        this.service = service;
    }

    /**
     * Cria um incidente ativo que sera rastreado por 30 minutos.
     * Body: { tipo, linha, tipoLinha, latitude, longitude }
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> criar(@RequestBody Map<String, Object> body) {
        String tipo = (String) body.get("tipo");
        Integer linha = body.get("linha") != null ? ((Number) body.get("linha")).intValue() : null;
        String tipoLinha = (String) body.get("tipoLinha");
        double lat = ((Number) body.get("latitude")).doubleValue();
        double lon = ((Number) body.get("longitude")).doubleValue();

        String id = service.criarIncidente(tipo, linha, tipoLinha, lat, lon);
        return new ResponseEntity<>(Map.of("id", id), HttpStatus.CREATED);
    }

    /**
     * Atualiza a coordenada do reporter (chamado pelo watchPosition do front).
     * Body: { latitude, longitude }
     */
    @PostMapping("/{id}/coordenadas")
    public ResponseEntity<Void> atualizarCoordenada(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        double lat = ((Number) body.get("latitude")).doubleValue();
        double lon = ((Number) body.get("longitude")).doubleValue();

        boolean ok = service.adicionarCoordenada(id, lat, lon);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Map<String, Object>> listarAtivos() {
        return service.listarAtivos();
    }

    /**
     * Remove um incidente (ex: usuario saiu da rota ou cancelou).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable String id) {
        service.removerIncidente(id);
        return ResponseEntity.noContent().build();
    }
}
