package com.transcol.busafe.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.repository.RotaRepository;
import com.transcol.busafe.model.PontoOnibus;

@RestController

@RequestMapping("/api/rotas")
public class RotaController {

    private final RotaRepository rotaRepo;

    // Removemos o PontoRotaRepository daqui, pois os pontos estão embutidos na Rota
    public RotaController(RotaRepository rotaRepo) {
        this.rotaRepo = rotaRepo;
    }


//api que retorna pelo número do ônibus

    @GetMapping("/buscar/{codigo}")
    public Map<String, Object> buscar(@PathVariable String codigo) {
        try {
            codigo = (codigo == null) ? "" : codigo.trim();

            // Busca as rotas. No MongoDB, cada 'rota' já traz sua lista 'pontoRota'
            List<Rota> rotas = rotaRepo.findAllByLinhaTranscolOrLinhaMunicipal(codigo, codigo);

            List<Map<String, Object>> features = new ArrayList<>();
            
        for (Rota rota : rotas) {
            var pts = rota.getPontos();
            
            // Log de segurança
            if (pts == null || pts.isEmpty()) {
                System.out.println("DEBUG: Rota " + rota.getLinhaTranscol() + " está sem pontos.");
                continue;
            }

            // Mapeamento filtrado: ignora pontos com coordenadas nulas
            var coords = pts.stream()
                .filter(p -> p.getLon() != null && p.getLat() != null) // Filtra nulos
                .sorted((p1, p2) -> {
                    if (p1.getOrdem() == null) return 1;
                    if (p2.getOrdem() == null) return -1;
                    return p1.getOrdem().compareTo(p2.getOrdem());
                })
                .map(p -> List.of(p.getLon(), p.getLat()))
                .collect(Collectors.toList());

            if (coords.isEmpty()) continue; // Se após o filtro não sobrou nada, pula

            Map<String, Object> props = new HashMap<>();
            props.put("linha_transcol", rota.getLinhaTranscol() != null ? rota.getLinhaTranscol() : "");
            props.put("linha_municipal", rota.getLinhaMunicipal() != null ? rota.getLinhaMunicipal() : "");
            props.put("sentido", rota.getSentido() != null ? rota.getSentido() : "");
            props.put("nome", rota.getNome() != null ? rota.getNome() : "");

            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");
            feature.put("properties", props);
            feature.put("geometry", Map.of(
                    "type", "LineString",
                    "coordinates", coords
            ));

            features.add(feature);
        }

            return Map.of("type", "FeatureCollection", "features", features);

        } catch (Exception e) {
            return emptyFC();
        }
    }

    @GetMapping("/sugestoes")
    public List<Rota> getRotaSugestoes(@RequestParam String termo) {
        if (termo == null || termo.length() < 1) {
            return List.of();
        }

        // Agora usamos o método que não traz os pontos, economizando banda e memória
        List<Rota> rotas = rotaRepo.findSuggestionsWithoutPoints(termo, termo);

        return rotas.stream()
            .collect(Collectors.toMap(
                r -> r.getLinhaTranscol() != null ? r.getLinhaTranscol() : r.getLinhaMunicipal(),
                r -> r,
                (primeiro, duplicado) -> primeiro,
                LinkedHashMap::new
            ))
            .values()
            .stream()
            .limit(5) // Garante o Top 5 após a deduplicação
            .toList();
    }

    private Map<String, Object> emptyFC() {
        return Map.of("type", "FeatureCollection", "features", List.of());
    }
}