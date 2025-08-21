package com.transcol.busafe.repository;

import com.transcol.busafe.model.PontoRota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PontoRotaRepository extends JpaRepository<PontoRota, Long> {
    List<PontoRota> findByLinhaTranscol(String linhaTranscol);
    List<PontoRota> findByLinhaMunicipal(String linhaMunicipal);
}
