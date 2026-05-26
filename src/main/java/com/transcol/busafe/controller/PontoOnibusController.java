package com.transcol.busafe.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transcol.busafe.model.PontoOnibus;
import com.transcol.busafe.repository.PontoOnibusRepository;

@RestController
@RequestMapping("/api/pontos")
public class PontoOnibusController {

    @Autowired
    private PontoOnibusRepository repository;

    @GetMapping("/proximos")
    public List<PontoOnibus> getPontosProximos(@RequestParam double lat, @RequestParam double lon) {
        // Busca os pontos num raio de 50 metros do clique
        return repository.findPontosProximos(lon, lat);
    }

    @GetMapping("/todos")
    public List<PontoOnibus> getAllPontos() {
        return repository.findAll();
    }

    @GetMapping("/info")
    public ResponseEntity<PontoOnibus> getInfoPonto(@RequestParam double lat, @RequestParam double lon) {
        // Usamos o seu repositório para buscar o ponto mais próximo da coordenada clicada
        // Ajuste "findPontosProximos" se ele retornar uma lista, para pegar o primeiro da lista
        List<PontoOnibus> pontos = repository.findPontosProximos(lon, lat);
        
        if (pontos != null && !pontos.isEmpty()) {
            return ResponseEntity.ok(pontos.get(0)); // Retorna o ponto mais próximo encontrado
        }
        return ResponseEntity.notFound().build();
    }
}