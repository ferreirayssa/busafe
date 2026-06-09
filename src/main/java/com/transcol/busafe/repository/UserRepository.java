package com.transcol.busafe.repository;

import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.TipoUsuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    // --- BUSCAS POR IDENTIFICADORES ---
    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
    
    // CORRIGIDO: Usando @Query para evitar ambiguidade
    @Query("{ 'cnpj' : ?0 }")
    Optional<User> buscarPorCnpj(String cnpj);
    
    // Busca por email, CPF ou CNPJ (para login)
    @Query("{ '$or' : [ { 'email' : ?0 }, { 'cpf' : ?1 }, { 'cnpj' : ?2 } ] }")
    Optional<User> findByEmailOrCpfOrCnpj(String email, String cpf, String cnpj);
    
    // --- VERIFICAÇÕES DE EXISTÊNCIA ---
    boolean existsByEmail(String email);
    boolean existsByCpf(String cpf);
    
    @Query(value = "{ 'cnpj' : ?0 }", exists = true)
    boolean existsByCnpj(String cnpj);
    
    // --- BUSCAS PARA VINCULADOS (PJ) ---
    @Query("{ 'cnpj' : ?0 }")
    List<User> listarPorCnpj(String cnpj);
    
    @Query("{ 'cnpj' : ?0, 'ativo' : true }")
    List<User> listarPorCnpjAtivos(String cnpj);
    
    @Query("{ 'contaPaiId' : ?0 }")
    List<User> findByContaPaiId(String contaPaiId);
    
    @Query("{ 'contaPaiId' : ?0, 'ativo' : true }")
    List<User> findByContaPaiIdAndAtivoTrue(String contaPaiId);
    
    // --- BUSCAS POR TIPO DE USUÁRIO ---
    List<User> findByTipoUsuario(TipoUsuario tipoUsuario);
    
    @Query("{ 'tipoUsuario' : ?0, 'ativo' : true }")
    List<User> findByTipoUsuarioAndAtivoTrue(TipoUsuario tipoUsuario);
    
    // --- BUSCAS POR PLANO ---
    List<User> findByPlano(String plano);
    List<User> findByPlanoAndAtivoTrue(String plano);
    
    // --- CONTAGENS ---
    long count();
    long countByAtivoTrue();
    long countByTipoUsuario(TipoUsuario tipoUsuario);
    long countByPlano(String plano);
    
    // --- BUSCAS POR STATUS ---
    List<User> findByAtivoTrue();
    List<User> findByAtivoFalse();
}