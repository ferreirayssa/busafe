//api que exibe todas as linhas

package com.transcol.busafe.controller;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.repository.RotaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rotas")
public class KmlController {

    private final RotaRepository rotaRepo;

    public KmlController(RotaRepository rotaRepo) {
        this.rotaRepo = rotaRepo;
    }

    @GetMapping
    public List<Rota> getAllRotas() {
        return rotaRepo.findAll();
    }

    @GetMapping("/{codigo}")
    public Rota getRotaByCodigo(@PathVariable String codigo) {
        return rotaRepo.findFirstByLinhaTranscolOrderByIdAsc(codigo);
    }

    @GetMapping("/geojson")
    public Map<String, Object> getAllRotasGeoJson() {
        List<Rota> rotas = rotaRepo.findAll();
        return Map.of("type", "FeatureCollection", "features", rotas);
    }
}
