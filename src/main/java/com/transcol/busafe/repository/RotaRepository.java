package com.transcol.busafe.repository;

import com.transcol.busafe.model.Rota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RotaRepository extends JpaRepository<Rota, Long> {
    Rota findFirstByLinhaTranscolOrderByIdAsc(String codigo);
    Rota findFirstByLinhaTranscolAndSentidoIgnoreCaseOrderByIdAsc(String codigo, String sentido);
    Rota findFirstByLinhaMunicipalOrderByIdAsc(String codigo);
    Rota findFirstByLinhaMunicipalAndSentidoIgnoreCaseOrderByIdAsc(String codigo, String sentido);

    boolean existsByLinhaTranscol(String codigo);
    boolean existsByLinhaMunicipal(String codigo);
}
