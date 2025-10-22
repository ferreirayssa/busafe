package com.transcol.busafe.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "relato")
public class Relato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "double precision", nullable = false)
    private Double latitude;

    @Column(columnDefinition = "double precision", nullable = false)
    private Double longitude;

    @Column(name = "data_relato", nullable = false, updatable = false)
    private LocalDateTime dataRelato;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "linha_transcol")
    private Integer linhaTranscol;

    @Column(name = "linha_municipal")
    private Integer linhaMunicipal;

    @PrePersist
    protected void onCreate() {
        this.dataRelato = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relato relato = (Relato) o;
        return id != null && Objects.equals(id, relato.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}