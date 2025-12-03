package com.transcol.busafe.infra.security;

import com.transcol.busafe.repository.UsuarioRepository;
import com.transcol.busafe.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var token = this.recoverToken(request);
        
        if(token != null){
            // Valida o token e recupera o login (CPF ou CNPJ)
            var login = tokenService.validateToken(token);
            
            if(!login.isEmpty()){
                // Busca o usuário no banco pelo login recuperado do token
                // Usamos o método genérico loadUserByUsername que implementamos via AuthorizationService
                // Ou podemos buscar direto no repositório. Como o repository tem findByLoginIdentifier customizado:
                UserDetails user = usuarioRepository.findByLoginIdentifier(login);

                if(user != null){
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}