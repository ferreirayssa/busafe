package com.transcol.busafe.controller;

import com.transcol.busafe.model.PontoRota;
import com.transcol.busafe.service.PontoRotaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rotas")
public class PontoRotaController {

    private final PontoRotaService service;

    public PontoRotaController(PontoRotaService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontoRota> listarRotas() {
        return service.listarTudo();
    }

    @GetMapping("/transcol/{linha}")
    public List<PontoRota> buscarPorLinhaTranscol(@PathVariable String linha) {
        return service.buscarPorLinhaTranscol(linha);
    }

    @GetMapping("/municipal/{linha}")
    public List<PontoRota> buscarPorLinhaMunicipal(@PathVariable String linha) {
        return service.buscarPorLinhaMunicipal(linha);
    }
}
