package com.transcol.busafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transcol.busafe.model.Relato;
import com.transcol.busafe.repository.RelatoRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/relatos")
public class RelatoController {

    private final RelatoRepository relatoRepo;

    public RelatoController(RelatoRepository relatoRepo) {
        this.relatoRepo = relatoRepo;
    }

    // Criar um novo relato (POST)
    @PostMapping
    public ResponseEntity<Relato> criarRelato(@Valid @RequestBody Relato relato) {
        // Como definimos a dataRelato = LocalDateTime.now() na Model, 
        // ela será salva automaticamente se não for enviada no JSON.
        Relato novoRelato = relatoRepo.save(relato);
        return new ResponseEntity<>(novoRelato, HttpStatus.CREATED);
    }

    // Listar todos os relatos (GET)
    @GetMapping
    public List<Relato> listarTodos() {
        return relatoRepo.findAll();
    }

    // Buscar relatos por linha (GET)
    @GetMapping("/linha/{numero}")
    public List<Relato> buscarPorLinha(@PathVariable Integer numero) {
        return relatoRepo.findByLinhaTranscol(numero);
    }
}
