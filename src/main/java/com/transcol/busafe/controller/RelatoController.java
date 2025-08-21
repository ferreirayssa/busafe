package com.transcol.busafe.controller;

import com.transcol.busafe.model.Relato;
import com.transcol.busafe.service.RelatoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/relatos")
public class RelatoController {

    private final RelatoService service;
    public RelatoController(RelatoService service) { this.service = service; }

    @PostMapping
    public Relato criar(@RequestBody Relato relato) {
        return service.salvar(relato); // dataRelato já vem default
    }

    @GetMapping
    public List<Relato> listar() {
        return service.listar();
    }
}
