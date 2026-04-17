package com.transcol.busafe.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
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

    /**
     * Relação Embedding para Favoritos:
     * Armazena apenas o código/número da linha (ex: "505").
     * Como sua RotaController já busca por código, isso facilita o carregamento.
     */
    private List<String> rotasFavoritas = new ArrayList<>();

    /**
     * Relação Embedding para Relatos:
     * Armazena os IDs dos relatos criados por este usuário.
     * Isso permite que o backend saiba a autoria sem expor no front-end.
     */
    private List<String> relatosIds = new ArrayList<>();
}