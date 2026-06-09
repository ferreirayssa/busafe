package com.transcol.busafe.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;
import com.transcol.busafe.model.enums.TipoUsuario;
import com.transcol.busafe.model.enums.Role;
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

    @Indexed(unique = true, sparse = true)
    private String cpf;

    @Indexed(unique = true, sparse = true)
    private String cnpj;

    private TipoUsuario tipoUsuario = TipoUsuario.PESSOA_FISICA; //erro aqui

    private String phone;

    private String password;

    private String plano = "FREE";

    private String contaPaiId; 

    private List<String> contasVinculadasIds = new ArrayList<>();

    private Role role = Role.USER; //erro aqui

    @Field("Favoritos")
    private List<Rota> rotasFavoritas = new ArrayList<>();

    @Field("Relatos")
    private List<String> relatos = new ArrayList<>();
}