package com.transcol.busafe.model;

import jakarta.persistence.*;

@Entity @Table(name="rota_ponto")
public class PontoRota {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="rota_id", nullable=false)
  private Rota rota;
  private Integer ordem;
  private Double lat;
  private Double lon;

  public Long getId() { return id; }
  public Rota getRota() { return rota; }
  public void setRota(Rota r) { this.rota = r; }
  public Integer getOrdem() { return ordem; }
  public void setOrdem(Integer o) { this.ordem = o; }
  public Double getLat() { return lat; }
  public void setLat(Double v) { this.lat = v; }
  public Double getLon() { return lon; }
  public void setLon(Double v) { this.lon = v; }
}
