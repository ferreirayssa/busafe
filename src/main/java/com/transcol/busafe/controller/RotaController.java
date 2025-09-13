package com.transcol.busafe.controller;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.repository.RotaRepository;
import com.transcol.busafe.repository.PontoRotaRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rotas")  // Mapeamento correto do Controller
public class RotaController {

    private final RotaRepository rotaRepo;
    private final PontoRotaRepository pontoRepo;

    public RotaController(RotaRepository rotaRepo, PontoRotaRepository pontoRepo) {
        this.rotaRepo = rotaRepo;
        this.pontoRepo = pontoRepo;
    }

    // Método de busca, agora unificado em um único endpoint
    @GetMapping("/buscar/{codigo}")
    public Map<String, Object> buscar(@PathVariable String codigo,
                                      @RequestParam(required = false) String sentido) {
        try {
            codigo = codigo == null ? "" : codigo.trim();

            // 1) Prioriza Transcol se existir; senão tenta Municipal
            Rota rota = null;
            if (rotaRepo.existsByLinhaTranscol(codigo)) {
                rota = (sentido == null || sentido.isBlank())
                        ? rotaRepo.findFirstByLinhaTranscolOrderByIdAsc(codigo)
                        : rotaRepo.findFirstByLinhaTranscolAndSentidoIgnoreCaseOrderByIdAsc(codigo, sentido);
            }
            // 2) Se não encontrar, tenta como Municipal
            if (rota == null && rotaRepo.existsByLinhaMunicipal(codigo)) {
                rota = (sentido == null || sentido.isBlank())
                        ? rotaRepo.findFirstByLinhaMunicipalOrderByIdAsc(codigo)
                        : rotaRepo.findFirstByLinhaMunicipalAndSentidoIgnoreCaseOrderByIdAsc(codigo, sentido);
            }
            // 3) Se não encontrar, tenta de qualquer jeito
            if (rota == null) {
                rota = rotaRepo.findFirstByLinhaTranscolOrderByIdAsc(codigo);
                if (rota == null) rota = rotaRepo.findFirstByLinhaMunicipalOrderByIdAsc(codigo);
            }

            if (rota == null) return emptyFC();

            var pts = pontoRepo.findByRotaIdOrderByOrdemAsc(rota.getId());
            if (pts.isEmpty()) return emptyFC();

            var coords = pts.stream().map(p -> List.of(p.getLon(), p.getLat())).toList();

            // Cria o FeatureCollection
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

            return Map.of("type", "FeatureCollection", "features", List.of(feature));

        } catch (Exception e) {
            // Log de erro (opcional)
            return emptyFC(); // Retorna vazio em caso de erro para não quebrar a aplicação
        }
    }

    // Função de fallback, retorna uma FeatureCollection vazia (200 OK, não erro)
    private Map<String, Object> emptyFC() {
        return Map.of("type", "FeatureCollection", "features", List.of());
    }

    // Manter métodos antigos para compatibilidade
    @GetMapping("/transcol/{codigo}")
    public Map<String, Object> rotaTranscol(@PathVariable String codigo,
                                            @RequestParam(required = false) String sentido) {
        return buscar(codigo, sentido);
    }

    @GetMapping("/municipal/{codigo}")
    public Map<String, Object> rotaMunicipal(@PathVariable String codigo,
                                             @RequestParam(required = false) String sentido) {
        return buscar(codigo, sentido);
    }
}
