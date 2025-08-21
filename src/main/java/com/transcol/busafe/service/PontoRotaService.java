package com.transcol.busafe.service;

import com.transcol.busafe.model.PontoRota;
import com.transcol.busafe.repository.PontoRotaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PontoRotaService {

    private final PontoRotaRepository repository;

    public PontoRotaService(PontoRotaRepository repository) {
        this.repository = repository;
    }

    public List<PontoRota> listarTudo() {
        return repository.findAll();
    }

    public List<PontoRota> buscarPorLinha(String linha) {
        return repository.findByLinhaTranscol(linha);
    }

    public List<PontoRota> buscarPorLinhaTranscol(String linha) {
    return repository.findByLinhaTranscol(linha);
    }

    public List<PontoRota> buscarPorLinhaMunicipal(String linha) {
    return repository.findByLinhaMunicipal(linha);
    }
}
