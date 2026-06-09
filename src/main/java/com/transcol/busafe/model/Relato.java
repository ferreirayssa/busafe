package com.transcol.busafe.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "relato")
public class Relato {

    @Id
    private String id;

    private String descricao;

    private String tipo;

    private Double latitude;

    private Double longitude;

    @Field("dataRelato")
    private LocalDateTime dataRelato = LocalDateTime.now();

    private String bairro;

    private String municipio;

    @Field("linhaMunicipal")
    private Integer linhaMunicipal;

    @Field("linhaTranscol")
    private Integer linhaTranscol;
    
    // ADICIONE ESTE CAMPO:
    @Field("usuarioId")
    private String usuarioId; // ID do usuário que fez o relato
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relato relato = (Relato) o;
        return id != null && id.equals(relato.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}