package com.transcol.busafe.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    private List<PontoRota> pontoRota;

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLinhaTranscol() { return linhaTranscol; }
    public void setLinhaTranscol(String v) { this.linhaTranscol = v; }

    public String getLinhaMunicipal() { return linhaMunicipal; }
    public void setLinhaMunicipal(String v) { this.linhaMunicipal = v; }

    public String getSentido() { return sentido; }
    public void setSentido(String v) { this.sentido = v; }

    public String getNome() { return nome; }
    public void setNome(String v) { this.nome = v; }

    public List<PontoRota> getPontoRota() { return pontoRota; }
    public void setPontoRota(List<PontoRota> pontoRota) { this.pontoRota = pontoRota; }
}