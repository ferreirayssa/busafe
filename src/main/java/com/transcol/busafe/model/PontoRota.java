package com.transcol.busafe.model;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data // Se não quiser usar Lombok, mantenha os Getters/Setters manuais
public class PontoRota {
    
    // No MongoDB, objetos aninhados em listas geralmente não precisam de um @Id próprio, 
    // a menos que você precise referenciá-los individualmente via API.
    private String id; 

    private Integer ordem;
    
    // Mapeando para 'lat' e 'lon' conforme seu JSON/Classe original
    private Double lat;
    private Double lon;

    @Field("rota_id")
    private Long rotaId;

    /* IMPORTANTE: 
       Removemos o 'private Rota rota;' daqui. 
       No NoSQL, a relação é implícita: se o ponto está dentro da lista de uma Rota, 
       ele já "sabe" a quem pertence. Evitamos referências circulares.
    */
}