package com.transcol.busafe.repository;

import com.transcol.busafe.model.Relato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatoRepository extends JpaRepository<Relato, Long> {}
