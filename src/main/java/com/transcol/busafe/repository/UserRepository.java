package com.transcol.busafe.repository;

import com.transcol.busafe.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByCpf(String cpf); // Adicione se não tiver
    Optional<User> findByEmail(String email);
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);

    long count();
}