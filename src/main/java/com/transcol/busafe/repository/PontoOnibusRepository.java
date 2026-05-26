package com.transcol.busafe.repository;

import com.transcol.busafe.model.PontoOnibus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface PontoOnibusRepository extends MongoRepository<PontoOnibus, String> {

    @Query("{ 'localizacao': { $near: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: 50 } } }")
    List<PontoOnibus> findPontosProximos(double lon, double lat);
}