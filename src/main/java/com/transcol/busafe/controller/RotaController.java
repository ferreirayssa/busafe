//api que exibe coordenadas das rotas em formato GeoJSON

package com.transcol.busafe.controller;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.repository.RotaRepository;
import com.transcol.busafe.repository.PontoRotaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rotas")

public class RotaController {

    private final RotaRepository rotaRepo;
    private final PontoRotaRepository pontoRepo;

    public RotaController(RotaRepository rotaRepo, PontoRotaRepository pontoRepo) {
        this.rotaRepo = rotaRepo;
        this.pontoRepo = pontoRepo;
    }

    // Método de busca, agora unificado em um único endpoint
    @GetMapping("/buscar/{codigo}")
    public Map<String, Object> buscar(@PathVariable String codigo) {
        try {
            codigo = codigo == null ? "" : codigo.trim();

            // Busca todas as rotas Transcol e Municipal com esse código, ignorando sentido
            List<Rota> rotas = rotaRepo.findAllByLinhaTranscolOrLinhaMunicipal(codigo, codigo);

            List<Map<String, Object>> features = new java.util.ArrayList<>();
            for (Rota rota : rotas) {
                var pts = pontoRepo.findByRotaIdOrderByOrdemAsc(rota.getId());
                if (pts.isEmpty()) continue;

                var coords = pts.stream().map(p -> List.of(p.getLon(), p.getLat())).toList();

                Map<String, Object> props = new java.util.HashMap<>();
                if (rota.getLinhaTranscol() != null) props.put("linha_transcol", rota.getLinhaTranscol());
                if (rota.getLinhaMunicipal() != null) props.put("linha_municipal", rota.getLinhaMunicipal());
                if (rota.getSentido() != null) props.put("sentido", rota.getSentido());
                if (rota.getNome() != null) props.put("nome", rota.getNome());
                if (rota.getPlacemarkName() != null) props.put("placemark", rota.getPlacemarkName());

                Map<String, Object> feature = new java.util.HashMap<>();
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

        // Busca original
        List<Rota> rotas = rotaRepo
                .findTop10DistinctByLinhaTranscolContainingIgnoreCaseOrLinhaMunicipalContainingIgnoreCase(
                        termo, termo
                );

        // --- Deduplicar usando stream ---
        return rotas.stream()
            .collect(Collectors.toMap(
                r -> r.getLinhaTranscol() != null
                        ? r.getLinhaTranscol().toString()
                        : r.getLinhaMunicipal().toString(),
                r -> r,
                (primeiro, duplicado) -> primeiro,      // mantém o primeiro
                LinkedHashMap::new                      // mantém a ordem original
            ))
            .values()
            .stream()
            .toList();
    }

    // Função de fallback, retorna uma FeatureCollection vazia (200 OK, não erro)
    private Map<String, Object> emptyFC() {
        return Map.of("type", "FeatureCollection", "features", List.of());
    }
}
