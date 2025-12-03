package com.transcol.busafe.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF (padrão para APIs Stateless/JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Não guarda sessão no servidor
                .authorizeHttpRequests(authorize -> authorize

                        // 🔓 ÁREA PÚBLICA (Qualquer um acessa)
                        // Login e Cadastros
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register/**").permitAll() // Libera /register/user, /register/company, etc.
                        
                        // Visualização de Rotas (Para atrair usuários para o app)
                        .requestMatchers(HttpMethod.GET, "/rotas/**").permitAll()

                        // Arquivos do Frontend (HTML, CSS, JS, Imagens, PWA)
                        .requestMatchers("/", "/index.html", "/html/**", "/css/**", "/js/**", "/Logos/**", "/manifest.json", "/service-worker.js").permitAll()

                        // 🔒 ÁREA RESTRITA (Precisa de Token JWT válido)
                        // Reportar incidentes (Qualquer usuário logado: USER, FUNCIONARIO, EMPRESA)
                        .requestMatchers(HttpMethod.POST, "/relatos").authenticated()

                        // Visualizar lista de relatos (Talvez apenas EMPRESA e ADMIN devam ver tudo?)
                        // Aqui deixei authenticated(), ou seja, todo mundo vê. Se quiser restringir: .hasRole("EMPRESA")
                        .requestMatchers(HttpMethod.GET, "/relatos").authenticated()

                        // Calcular Risco (Funcionalidade Premium/Logada)
                        .requestMatchers(HttpMethod.GET, "/risco").authenticated()

                        // 👮 ÁREA ADMINISTRATIVA (Exemplo futuro)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/rotas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/rotas").permitAll()

                        // Bloqueia qualquer outra rota não listada acima
                        .anyRequest().authenticated()
                )
                // Adiciona o nosso filtro de Token antes do filtro padrão do Spring
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}