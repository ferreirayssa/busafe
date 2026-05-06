package com.transcol.busafe.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer; // Importação necessária
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desabilita proteção contra CSRF (padrão para APIs REST)
            .cors(Customizer.withDefaults()) // Ativa as configurações de CORS do Controller (@CrossOrigin)
            .authorizeHttpRequests(auth -> auth
                // Libera o acesso sem autenticação para login e cadastro
                .requestMatchers("/api/users/login", "/api/users/register").permitAll()
                // Libera os demais endpoints por enquanto (para teste)
                .requestMatchers("/api/users/**").permitAll()
                // Qualquer outra rota do sistema
                .anyRequest().permitAll() 
            );
        
        return http.build();
    }
}