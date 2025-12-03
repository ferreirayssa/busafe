package com.transcol.busafe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterEmployeeDTO {

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "O CPF do funcionário é obrigatório")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "A senha é obrigatória")
    private String password;

    @NotBlank(message = "O CNPJ da empresa vinculada é obrigatório")
    @CNPJ(message = "CNPJ da empresa inválido")
    private String cnpjEmpresa;
}