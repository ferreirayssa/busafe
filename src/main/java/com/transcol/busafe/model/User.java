package com.transcol.busafe.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
public class User {

    @Id
    private String id;

    private String nome;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String cpf;

    private String phone; // Campo que recebe o DDI + número do seu front-end

    private String password;

    // Atributo solicitado: inicia como FREE e pode ser sobrescrito
    private String plano = "FREE";

    @Field("Favoritos")
    private List<Rota> rotasFavoritas = new ArrayList<>();

    @Field("Relatos")
    private List<String> relatos = new ArrayList<>();
}