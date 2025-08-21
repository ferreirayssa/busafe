package com.transcol.busafe.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class PontoRota {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String linhaMunicipal;
    private String linhaTranscol;
    private String sentido;
    private String logradouro;

    @Column(columnDefinition = "real")
    private Float lat;

    @Column(columnDefinition = "real")
    private Float lon;
}
