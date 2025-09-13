package com.transcol.busafe.model;

import jakarta.persistence.*;

@Entity @Table(name="rota")

public class Rota {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="linha_transcol")  private String linhaTranscol;
  @Column(name="linha_municipal") private String linhaMunicipal;
  private String sentido;
  
  @Column(name="placemark_name")  private String placemarkName;
  @Column(name = "nome") private String nome;

  public Long getId() { return id; }
  public String getLinhaTranscol() { return linhaTranscol; }
  public void setLinhaTranscol(String v) { this.linhaTranscol = v; }
  public String getLinhaMunicipal() { return linhaMunicipal; }
  public void setLinhaMunicipal(String v) { this.linhaMunicipal = v; }
  public String getSentido() { return sentido; }
  public void setSentido(String v) { this.sentido = v; }
  public String getPlacemarkName() { return placemarkName; }
  public void setPlacemarkName(String v) { this.placemarkName = v; }
  public String getNome() { return nome; }
  public void setNome(String v) { this.nome = v; }
}
