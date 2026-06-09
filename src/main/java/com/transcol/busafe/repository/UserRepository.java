package com.transcol.busafe.repository;

import com.transcol.busafe.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
     Optional<User> findByEmailOrCpfOrCnpj(String loginEnviado, String loginEnviado2, String loginEnviado3);
    Optional<User> findByCpf(String cpf);
    Optional<User> findByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    boolean existsByCnpj(String cnpj);

    long count();
}