package com.transcol.busafe.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter

@Document(collection = "rota") // Define a coleção no MongoDB
public class Rota {

    @Id
    private String id; // No MongoDB, o padrão é usar String (ObjectId)

    @Field("linhaTranscol")
    private String linhaTranscol;

    @Field("linhaMunicipal")
    private String linhaMunicipal;

    private String sentido;

    private String nome;

    // Relacionamento por composição (Embebed Document)
    // Isso mapeia o array "pontoRota" que está no seu JSON
    @Field("pontos")
    private List<PontoRota> Pontos;
}