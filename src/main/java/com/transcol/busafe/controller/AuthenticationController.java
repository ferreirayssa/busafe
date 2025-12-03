package com.transcol.busafe.controller;

import com.transcol.busafe.dto.*; // Importa todos os DTOs
import com.transcol.busafe.model.Usuario;
import com.transcol.busafe.repository.UsuarioRepository;
import com.transcol.busafe.service.TokenService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UsuarioRepository repository;
    @Autowired private TokenService tokenService;

    // LOGIN UNIFICADO (CPF ou CNPJ)
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationDTO data){
        // MUDANÇA: data.getLogin() e data.getPassword()
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.getLogin(), data.getPassword());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((Usuario) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    // CADASTRO DE USUÁRIO COMUM (CPF)
    @PostMapping("/register/user")
    public ResponseEntity<String> registerUser(@RequestBody @Valid RegisterUserDTO data){
        if(this.repository.existsByCpf(data.getCpf())) return ResponseEntity.badRequest().body("CPF já cadastrado");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());
        // MUDANÇA: data.getEmail(), data.getCpf()...
        Usuario newUser = new Usuario(data.getEmail(), data.getCpf(), encryptedPassword, "USER");

        this.repository.save(newUser);
        return ResponseEntity.ok().build();
    }

    // CADASTRO DE EMPRESA (CNPJ)
    @PostMapping("/register/company")
    public ResponseEntity<String> registerCompany(@RequestBody @Valid RegisterCompanyDTO data){
        if(this.repository.existsByCnpj(data.getCnpj())) return ResponseEntity.badRequest().body("CNPJ já cadastrado");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());
        
        Usuario newCompany = new Usuario();
        newCompany.setEmail(data.getEmail());
        newCompany.setCnpj(data.getCnpj());
        newCompany.setPassword(encryptedPassword);
        newCompany.setRole("EMPRESA");

        this.repository.save(newCompany);
        return ResponseEntity.ok().build();
    }

    // CADASTRO DE FUNCIONÁRIO VINCULADO (Regra de 50)
    @PostMapping("/register/employee")
    public ResponseEntity<String> registerEmployee(@RequestBody @Valid RegisterEmployeeDTO data){
        // 1. Achar a empresa usando o getCnpjEmpresa()
        Usuario empresa = (Usuario) this.repository.findByCnpj(data.getCnpjEmpresa());
        if(empresa == null) return ResponseEntity.badRequest().body("Empresa não encontrada");

        // 2. Verificar limite de 50 contas
        long funcionariosAtuais = this.repository.countByEmpresa(empresa);
        if(funcionariosAtuais >= 50) {
            return ResponseEntity.badRequest().body("Limite de 50 funcionários atingido para esta empresa.");
        }

        if(this.repository.existsByCpf(data.getCpf())) return ResponseEntity.badRequest().body("CPF já cadastrado");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.getPassword());
        
        Usuario funcionario = new Usuario();
        funcionario.setEmail(data.getEmail());
        funcionario.setCpf(data.getCpf());
        funcionario.setPassword(encryptedPassword);
        funcionario.setRole("FUNCIONARIO");
        funcionario.setEmpresa(empresa); // Vínculo criado aqui

        this.repository.save(funcionario);
        return ResponseEntity.ok().build();
    }
}