package com.transcol.busafe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "usuarios")
@Entity(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String email;

    // Apenas para usuários PF (Pessoa Física)
    @Column(unique = true)
    private String cpf;

    // Apenas para Empresas
    @Column(unique = true)
    private String cnpj;

    private String password;

    // Roles: ADMIN, USER, EMPRESA, FUNCIONARIO
    private String role; 

    // Auto-relacionamento: Um funcionário pertence a uma empresa (que também é um Usuario)
    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Usuario empresa;

    // Construtor para Usuário Comum (CPF)
    public Usuario(String email, String cpf, String password, String role) {
        this.email = email;
        this.cpf = cpf;
        this.password = password;
        this.role = role;
    }

    // Construtor para Empresa (CNPJ)
    public Usuario(String email, String cnpj, String password, String role, boolean isEmpresa) {
        this.email = email;
        this.cnpj = cnpj;
        this.password = password;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if ("ADMIN".equals(this.role)) return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        if ("EMPRESA".equals(this.role)) return List.of(new SimpleGrantedAuthority("ROLE_EMPRESA"));
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // O "username" do Spring Security será dinâmico na lógica de serviço
    @Override
    public String getUsername() {
        return this.cpf != null ? this.cpf : this.cnpj;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}