package com.transcol.busafe.service;

import com.transcol.busafe.model.Relato;
import com.transcol.busafe.repository.RelatoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RelatoService {
    private final RelatoRepository repo;
    public RelatoService(RelatoRepository repo) { this.repo = repo; }

    public Relato salvar(Relato r) { return repo.save(r); }
    public List<Relato> listar() { return repo.findAll(); }

    public List<Relato> listarTodos() {
        return repo.findAll();
    }
}
