package com.transcol.busafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transcol.busafe.model.Relato;
import com.transcol.busafe.repository.RelatoRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/relatos")
public class RelatoController {

    private final RelatoRepository relatoRepo;

    public RelatoController(RelatoRepository relatoRepo) {
        this.relatoRepo = relatoRepo;
    }

    // Criar um novo relato (POST)
    @PostMapping
    public ResponseEntity<Relato> criarRelato(@RequestBody Relato relato) {
        Relato novoRelato = relatoRepo.save(relato);
        return new ResponseEntity<>(novoRelato, HttpStatus.CREATED);
    }

    // Listar todos os relatos (GET)
    @GetMapping
    public List<Relato> listarTodos() {
        return relatoRepo.findAll();
    }

    // Buscar relatos por linha Transcol (GET)
    @GetMapping("/linha/{numero}")
    public ResponseEntity<List<Relato>> buscarPorLinha(@PathVariable Integer numero) {
        List<Relato> relatos = relatoRepo.findByLinhaTranscol(numero);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }

    // Buscar relatos por linha Municipal
    @GetMapping("/municipal/{numero}")
    public ResponseEntity<List<Relato>> buscarPorLinhaMunicipal(@PathVariable Integer numero) {
        List<Relato> relatos = relatoRepo.findByLinhaMunicipal(numero);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }

    // Buscar relatos por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Relato>> buscarPorTipo(@PathVariable String tipo) {
        List<Relato> relatos = relatoRepo.findByTipo(tipo);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }

    // Buscar relatos por município
    @GetMapping("/municipio/{municipio}")
    public ResponseEntity<List<Relato>> buscarPorMunicipio(@PathVariable String municipio) {
        List<Relato> relatos = relatoRepo.findByMunicipio(municipio);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }
}