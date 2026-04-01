package com.transcol.busafe.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.transcol.busafe.model.Rota;

@Repository
public interface RotaRepository extends MongoRepository<Rota, String> {

    // O Spring Data MongoDB traduz esses nomes de método automaticamente
    Rota findFirstByLinhaTranscolOrderByIdAsc(String codigo);
    
    Rota findFirstByLinhaTranscolAndSentidoIgnoreCaseOrderByIdAsc(String codigo, String sentido);
    
    Rota findFirstByLinhaMunicipalOrderByIdAsc(String codigo);
    
    Rota findFirstByLinhaMunicipalAndSentidoIgnoreCaseOrderByIdAsc(String codigo, String sentido);

    boolean existsByLinhaTranscol(String codigo);
    boolean existsByLinhaMunicipal(String codigo);

    List<Rota> findAllByLinhaTranscolOrLinhaMunicipal(String transcol, String municipal);

    // O MongoDB lida muito bem com buscas 'Containing' e 'IgnoreCase'
    List<Rota> findTop10DistinctByLinhaTranscolContainingIgnoreCaseOrLinhaMunicipalContainingIgnoreCase(String termoTranscol, String termoMunicipal);

<<<<<<< rayssaf
    @Query(value = "{ 'linhaTranscol': ?0 }", fields = "{ 'pontos': 1, '_id': 0 }")
    Rota findPointsByLinhaTranscol(String codigo);

    @Query(value = "{ '$or': [ " +
                "{ 'linhaTranscol': { '$regex': ?0, '$options': 'i' } }, " +
                "{ 'linhaMunicipal': { '$regex': ?1, '$options': 'i' } } " +
                "] }", 
        fields = "{ 'pontos': 0 }")
=======
    @Query(value = "{ 'linha_transcol': ?0 }", fields = "{ 'pontoRota': 1, '_id': 0 }")
    Rota findPointsByLinhaTranscol(String codigo);

    @Query(value = "{ $or: [ { 'linha_transcol': { $regex: ?0, $options: 'i' } }, { 'linha_municipal': { $regex: ?1, $options: 'i' } } ] }", 
           fields = "{ 'pontoRota': 0 }")
>>>>>>> main
    List<Rota> findSuggestionsWithoutPoints(String termoTranscol, String termoMunicipal);
}