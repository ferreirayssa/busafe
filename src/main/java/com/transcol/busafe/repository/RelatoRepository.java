package com.transcol.busafe.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.transcol.busafe.model.Relato;

@Repository
public interface RelatoRepository extends MongoRepository<Relato, String> {
    
    // Você pode adicionar buscas customizadas que o MongoDB resolve sozinho:
    
    // Buscar relatos por bairro
    List<Relato> findByBairroIgnoreCase(String bairro);
    
    // Buscar relatos de uma linha específica (Transcol ou Municipal)
    List<Relato> findByLinhaTranscol(Integer linhaTranscol);
    List<Relato> findByLinhaMunicipal(Integer linhaMunicipal);
    
    // Buscar relatos recentes (o Spring Data Mongo entende o 'After')
    // List<Relato> findByDataRelatoAfter(LocalDateTime data);
}