package com.transcol.busafe.model;

import com.transcol.busafe.model.enums.Plano;
import com.transcol.busafe.model.enums.TipoUsuario;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id; // MongoDB usa String

    private String nome;

    @Indexed(unique = true)
    private String email;

    private String password;

    @Indexed(unique = true, sparse = true) // sparse: ignora null
    private String cpf;

    @Indexed(unique = true, sparse = true) // sparse: ignora null
    private String cnpj;

    @Field("tipoUsuario")
    private TipoUsuario tipoUsuario; // Enum: PESSOA_FISICA, PESSOA_JURIDICA

    @Field("plano")
    private Plano plano; // Enum: FREE, INDIVIDUAL, EMPRESARIAL

    @Field("ativo")
    private boolean ativo = true; // CORRIGIDO: Campo adicionado

    @Field("contaPaiId")
    private String contaPaiId; // ID da empresa (para vinculados)

    @Field("empresaId")
    private String empresaId; // CORRIGIDO: Campo adicionado

    @Field("contasVinculadasIds")
    private List<String> contasVinculadasIds = new ArrayList<>(); // IDs dos vinculados (para PJ)

    @Field("rotasFavoritas")
    private List<Rota> rotasFavoritas = new ArrayList<>();

    // Métodos auxiliares
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // Para compatibilidade com código que usa setEmpresaId
    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    public String getEmpresaId() {
        return empresaId;
    }
}