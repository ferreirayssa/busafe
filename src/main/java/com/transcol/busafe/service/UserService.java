package com.transcol.busafe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.TipoUsuario;
import com.transcol.busafe.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional 
    public User salvarUsuario(User user) {
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        if (TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario())) {
            if (user.getCpf() == null || user.getCpf().isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para Pessoa Física.");
            }
            if (userRepository.existsByCpf(user.getCpf())) {
                throw new IllegalArgumentException("CPF já cadastrado.");
            }
            user.setCnpj(null);

        } else if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            if (user.getCnpj() == null || user.getCnpj().isBlank()) {
                throw new IllegalArgumentException("CNPJ é obrigatório para Pessoa Jurídica.");
            }
            if (userRepository.existsByCnpj(user.getCnpj())) {
                throw new IllegalArgumentException("CNPJ já cadastrado.");
            }
            user.setCpf(null);
            
            user.setContaPaiId(null); 
        } else {
            throw new IllegalArgumentException("Tipo de usuário inválido.");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getContaPaiId() != null && !user.getContaPaiId().isBlank()) {
            
            if (!TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario())) {
                throw new IllegalArgumentException("Apenas contas de Pessoa Física (PF) podem ser vinculadas a uma Empresa.");
            }

            Optional<User> contaPaiOpt = userRepository.findById(user.getContaPaiId());
            if (contaPaiOpt.isEmpty()) {
                throw new IllegalArgumentException("Conta principal (Empresa) não encontrada.");
            }
            
            User contaPai = contaPaiOpt.get();

            if (!TipoUsuario.PESSOA_JURIDICA.equals(contaPai.getTipoUsuario())) {
                throw new IllegalArgumentException("O vínculo só pode ser feito com uma conta de Empresa (PJ).");
            }

            User usuarioSalvo = userRepository.save(user);

            contaPai.getContasVinculadasIds().add(usuarioSalvo.getId());
            userRepository.save(contaPai);

            return usuarioSalvo;
        }

        return userRepository.save(user);
    }

    public long allUsers() {
        return userRepository.count();
    }
}