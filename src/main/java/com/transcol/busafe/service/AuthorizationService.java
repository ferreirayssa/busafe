package com.transcol.busafe.service;

import com.transcol.busafe.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements UserDetailsService {

    @Autowired
    UsuarioRepository repository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Tenta achar
        UserDetails user = repository.findByLoginIdentifier(identifier);
        
        if (user == null) {
            throw new UsernameNotFoundException("Usuário não encontrado com CPF/CNPJ: " + identifier);
        }
        return user;
    }
}