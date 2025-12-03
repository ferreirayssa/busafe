package com.transcol.busafe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Gera Getters, Setters, toString, equals, hashCode
@NoArgsConstructor // Gera construtor vazio
@AllArgsConstructor // Gera construtor com todos os argumentos
public class AuthenticationDTO {
    
    @NotBlank(message = "O login é obrigatório")
    private String login;
    
    @NotBlank(message = "A senha é obrigatória")
    private String password;
}