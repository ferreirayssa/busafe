package com.transcol.busafe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.DispatcherType;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .cors(cors -> cors.configure(http))
            .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API sem estado
            .authorizeHttpRequests(req -> {
            req.requestMatchers(HttpMethod.GET, "/").permitAll(); // Libera a sua Home

                req.requestMatchers("/html/**", "/css/**", "/js/**", "/images/**", "/Logos/**", "/prototipo/**", "/error/**").permitAll();

                req.requestMatchers(HttpMethod.POST, "/api/users/login").permitAll();
                req.requestMatchers(HttpMethod.POST, "/api/users/register").permitAll();
                req.requestMatchers(HttpMethod.POST, "/api/users/verify").permitAll();
                req.requestMatchers(HttpMethod.PUT, "/api/users/reset-password").permitAll();

                // --- ADICIONE ESTAS LINHAS DE VOLTA ---
                req.requestMatchers(HttpMethod.GET, "/api/pontos/**").permitAll();
                req.requestMatchers(HttpMethod.GET, "/api/rotas/**").permitAll();
                
                // Rotas PRIVADAS (Qualquer outra requisição precisará do token válido)
                req.anyRequest().authenticated();
            })
            .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        var configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("*"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
        var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}