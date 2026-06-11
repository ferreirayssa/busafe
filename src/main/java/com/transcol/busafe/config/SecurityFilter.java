package com.transcol.busafe.config;

import com.transcol.busafe.model.User;
import com.transcol.busafe.repository.UserRepository;
import com.transcol.busafe.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = recuperarToken(request);
            
            if (token != null) {
                // ATUALIZADO: Agora extraímos o E-MAIL de dentro do token, e não mais o CPF
                String emailUsuario = tokenService.getSubject(token);
                
                if (emailUsuario != null) {
                    // ATUALIZADO: Buscamos o usuário no banco pelo E-MAIL
                    Optional<User> userOpt = userRepository.findByEmail(emailUsuario);
                    
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (Exception e) {
            // Se o token estiver expirado ou a assinatura for inválida (SignatureException), 
            // limpamos o contexto. O Spring retornará 403 naturalmente e de forma limpa.
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}