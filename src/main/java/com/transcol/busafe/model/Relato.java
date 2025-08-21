package com.transcol.busafe.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class Relato {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "real")
    private Float latitude;

    @Column(columnDefinition = "real")
    private Float longitude;

    private LocalDateTime dataRelato = LocalDateTime.now();
    private String descricao;

    private String linhaTranscol;
    private String linhaMunicipal;
}
