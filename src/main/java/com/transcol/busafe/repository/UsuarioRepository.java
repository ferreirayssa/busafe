package com.transcol.busafe.repository;

import com.transcol.busafe.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    // Busca flexível para o login (Spring Security)
    UserDetails findByCpf(String cpf);
    UserDetails findByCnpj(String cnpj);
    
    // Verifica existência para evitar duplicidade no cadastro
    boolean existsByCpf(String cpf);
    boolean existsByCnpj(String cnpj);
    boolean existsByEmail(String email);

    // Conta quantos funcionários estão vinculados a uma empresa específica
    long countByEmpresa(Usuario empresa);
    
    // Query personalizada para buscar por qualquer identificador (útil para login genérico)
    @Query("SELECT u FROM usuarios u WHERE u.cpf = :login OR u.cnpj = :login")
    Usuario findByLoginIdentifier(String login);
}