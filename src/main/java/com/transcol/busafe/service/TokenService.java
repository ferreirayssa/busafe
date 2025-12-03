package com.transcol.busafe.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.transcol.busafe.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    
    // Idealmente, isso vem do application.properties
    // @Value("${api.security.token.secret}")
    private String secret = "minha-chave-super-secreta-busafe"; 

    public String generateToken(Usuario usuario){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            
            // O getUsername() do seu Usuario já retorna CPF ou CNPJ, 
            // conforme a lógica que definimos na entidade.
            String subject = usuario.getUsername();

            return JWT.create()
                    .withIssuer("auth-busafe") // Quem gerou
                    .withSubject(subject)      // Quem é o usuário (CPF ou CNPJ)
                    .withExpiresAt(genExpirationDate()) // Quando expira
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    public String validateToken(String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("auth-busafe")
                    .build()
                    .verify(token)
                    .getSubject(); // Retorna o CPF ou CNPJ escondido no token
        } catch (JWTVerificationException exception){
            // Retorna vazio se o token for inválido ou expirado
            return "";
        }
    }

    private Instant genExpirationDate(){
        // Define que o token expira em 2 horas
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}