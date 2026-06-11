package com.transcol.busafe.repository;

import com.transcol.busafe.model.Relato;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatoRepository extends MongoRepository<Relato, String> {
    
    List<Relato> findByUsuarioHash(String usuarioHash);

    List<Relato> findByUsuarioHashOrderByDataRelatoDesc(String usuarioHash);
    List<Relato> findByUsuarioHashInOrderByDataRelatoDesc(List<String> usuarioHashes);
    long countByUsuarioHash(String usuarioHash);
      
    // Buscar por tipo
    @Query("{ 'tipo' : ?0 }")
    List<Relato> findByTipo(String tipo);
    
    // Buscar por município
    @Query("{ 'municipio' : ?0 }")
    List<Relato> findByMunicipio(String municipio);
    
    // Buscar por linha
    @Query("{ 'linhaTranscol' : ?0 }")
    List<Relato> findByLinhaTranscol(Integer linhaTranscol);
    
    @Query("{ 'linhaMunicipal' : ?0 }")
    List<Relato> findByLinhaMunicipal(Integer linhaMunicipal);
    
    // Buscar por período
    @Query("{ 'dataRelato' : { $gte: ?0, $lte: ?1 } }")
    List<Relato> findByDataRelatoBetween(
        java.time.LocalDateTime inicio, 
        java.time.LocalDateTime fim
    );
}