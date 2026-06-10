package com.transcol.busafe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.Plano;
import com.transcol.busafe.model.enums.TipoUsuario;
import com.transcol.busafe.repository.UserRepository;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User salvarUsuario(User user) {
        
        validarEmail(user);
        validarTipoUsuario(user);
        validarDocumento(user);
        validarPlano(user);
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        if (user.getPlano() == null) {
            user.setPlano(Plano.FREE);
        }
        
        user.setAtivo(true);
        
        if (user.getContaPaiId() != null && !user.getContaPaiId().isBlank()) {
            return salvarUsuarioVinculado(user);
        }
        
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            user.setContasVinculadasIds(new ArrayList<>());
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public User salvarUsuarioVinculado(User user) {
        
        System.out.println("🔍 Salvando vinculado: " + user.getNome());
        System.out.println("   Email: " + user.getEmail());
        System.out.println("   CPF: " + user.getCpf());
        System.out.println("   ContaPaiId: " + user.getContaPaiId());
        
        if (!TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario())) {
            throw new IllegalArgumentException("Apenas contas de Pessoa Física (PF) podem ser vinculadas a uma Empresa.");
        }

        User contaPai = userRepository.findById(user.getContaPaiId())
                .orElseThrow(() -> new IllegalArgumentException("Conta principal (Empresa) não encontrada."));

        if (!TipoUsuario.PESSOA_JURIDICA.equals(contaPai.getTipoUsuario())) {
            throw new IllegalArgumentException("O vínculo só pode ser feito com uma conta de Empresa (PJ).");
        }

        user.setCnpj(contaPai.getCnpj());
        user.setPlano(contaPai.getPlano());
        
        User usuarioSalvo = userRepository.save(user);

        if (contaPai.getContasVinculadasIds() == null) {
            contaPai.setContasVinculadasIds(new ArrayList<>());
        }
        contaPai.getContasVinculadasIds().add(usuarioSalvo.getId());
        userRepository.save(contaPai);

        return usuarioSalvo;
    }

    private void validarEmail(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-mail é obrigatório.");
        }
        
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
    }

    private void validarTipoUsuario(User user) {
        if (user.getTipoUsuario() == null) {
            throw new IllegalArgumentException("Tipo de usuário é obrigatório.");
        }
        
        if (!TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario()) && 
            !TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            throw new IllegalArgumentException("Tipo de usuário inválido. Use PESSOA_FISICA ou PESSOA_JURIDICA.");
        }
    }

    private void validarDocumento(User user) {
        if (TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario())) {
            if (user.getCpf() == null || user.getCpf().isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para Pessoa Física.");
            }
            
            String cpfLimpo = user.getCpf().replaceAll("[^0-9]", "");
            user.setCpf(cpfLimpo);
            
            if (cpfLimpo.length() != 11) {
                throw new IllegalArgumentException("CPF inválido. Deve conter 11 dígitos.");
            }
            
            if (user.getContaPaiId() == null && userRepository.existsByCpf(cpfLimpo)) {
                throw new IllegalArgumentException("CPF já cadastrado.");
            }
            
            user.setCnpj(null);

        } else if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            if (user.getCnpj() == null || user.getCnpj().isBlank()) {
                throw new IllegalArgumentException("CNPJ é obrigatório para Pessoa Jurídica.");
            }
            
            String cnpjLimpo = user.getCnpj().replaceAll("[^0-9]", "");
            user.setCnpj(cnpjLimpo);
            
            if (cnpjLimpo.length() != 14) {
                throw new IllegalArgumentException("CNPJ inválido. Deve conter 14 dígitos.");
            }
            
            if (userRepository.existsByCnpj(cnpjLimpo)) {
                throw new IllegalArgumentException("CNPJ já cadastrado.");
            }
            
            user.setCpf(null);
            user.setContaPaiId(null);
        }
    }

    private void validarPlano(User user) {
        if (user.getPlano() == null) {
            user.setPlano(Plano.FREE);
            return;
        }
        
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario()) && 
            Plano.FREE.equals(user.getPlano())) {
            throw new IllegalArgumentException("Pessoa Jurídica não pode ter plano Free. Escolha Individual ou Empresarial.");
        }
        
        if (TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario()) && 
            Plano.EMPRESARIAL.equals(user.getPlano()) && 
            user.getContaPaiId() == null) {
            throw new IllegalArgumentException("Plano Empresarial disponível apenas para contas vinculadas a uma PJ.");
        }
    }

    @Transactional
    public User atualizarPlano(String userId, Plano novoPlano) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario()) && 
            Plano.FREE.equals(novoPlano)) {
            throw new IllegalArgumentException("Pessoa Jurídica não pode ter plano Free.");
        }
        
        user.setPlano(novoPlano);
        
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario()) && 
            user.getContasVinculadasIds() != null) {
            
            user.getContasVinculadasIds().forEach(vinculadoId -> {
                User vinculado = userRepository.findById(vinculadoId).orElse(null);
                if (vinculado != null) {
                    vinculado.setPlano(novoPlano);
                    userRepository.save(vinculado);
                }
            });
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public void desativarUsuario(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        user.setAtivo(false);
        userRepository.save(user);
    }

    @Transactional
    public void reativarUsuario(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        
        user.setAtivo(true);
        userRepository.save(user);
    }

    @Transactional
    public void removerVinculo(String empresaId, String vinculadoId) {
        User empresa = userRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada."));
        
        User vinculado = userRepository.findById(vinculadoId)
                .orElseThrow(() -> new IllegalArgumentException("Vinculado não encontrado."));
        
        if (!TipoUsuario.PESSOA_JURIDICA.equals(empresa.getTipoUsuario())) {
            throw new IllegalArgumentException("Apenas PJ pode remover vínculos.");
        }
        
        if (empresa.getContasVinculadasIds() != null) {
            empresa.getContasVinculadasIds().remove(vinculadoId);
            userRepository.save(empresa);
        }
        
        vinculado.setAtivo(false);
        vinculado.setContaPaiId(null);
        userRepository.save(vinculado);
    }

    public long allUsers() {
        return userRepository.countByAtivoTrue();
    }

    public Optional<User> buscarPorEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> buscarPorCpf(String cpf) {
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        return userRepository.findByCpf(cpfLimpo);
    }

    public Optional<User> buscarPorCnpj(String cnpj) {
        String cnpjLimpo = cnpj.replaceAll("[^0-9]", "");
        return userRepository.buscarPorCnpj(cnpjLimpo);
    }
}