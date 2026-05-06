package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // IMPORTANTE
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Injeta o encoder da sua SecurityConfig

    // --- CADASTRO ---
@PostMapping("/register")
public ResponseEntity<?> registrar(@RequestBody User user) {
    System.out.println("=== TENTATIVA DE REGISTRO ===");
    System.out.println("CPF recebido: " + user.getCpf());
    System.out.println("Email recebido: " + user.getEmail());

    try {
        // Verifica CPF
        if (userRepository.existsByCpf(user.getCpf())) {
            System.out.println("BLOQUEADO: CPF já existe no banco.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CPF já cadastrado.");
        }

        // Verifica Email
        if (userRepository.existsByEmail(user.getEmail())) {
            System.out.println("BLOQUEADO: E-mail já existe no banco.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail já cadastrado.");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        System.out.println("Salvando no MongoDB...");
        User novoUser = userRepository.save(user);
        System.out.println("SUCESSO! Usuário salvo com ID: " + novoUser.getId());
        
        return new ResponseEntity<>(novoUser, HttpStatus.CREATED);

    } catch (Exception e) {
        System.err.println("ERRO AO SALVAR: " + e.getMessage());
        e.printStackTrace(); 
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro no servidor: " + e.getMessage());
    }
}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String cpfEnviado = body.get("login"); 
        String senhaEnviada = body.get("password");

        Optional<User> userOpt = userRepository.findByCpf(cpfEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // COMPARAÇÃO COM BCRYPT (matches)
            if (passwordEncoder.matches(senhaEnviada, user.getPassword())) {
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("nome", user.getNome());
                response.put("plano", user.getPlano());
                response.put("token", "fake-jwt-token-" + user.getId());
                
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha incorretos.");
    }

    // --- GESTÃO DE PLANO (Sobrescrever) ---
    @PatchMapping("/{id}/plano")
    public ResponseEntity<?> atualizarPlano(@PathVariable String id, @RequestBody Map<String, String> body) {
        String novoPlano = body.get("plano");
        return userRepository.findById(id).map(user -> {
            user.setPlano(novoPlano);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- FAVORITAR ROTA (Pelo código/número da linha) ---
    @PostMapping("/{id}/favoritar-rota")
    public ResponseEntity<?> favoritarRota(@PathVariable String id, @RequestBody Map<String, String> body) {
        String codigoRota = body.get("codigo"); // Ex: "505"
        return userRepository.findById(id).map(user -> {
            if (!user.getRotasFavoritas().contains(codigoRota)) {
                user.getRotasFavoritas().add(codigoRota);
                userRepository.save(user);
            }
            return ResponseEntity.ok(user);
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- VINCULAR RELATO (Privado) ---
    @PostMapping("/{userId}/vincular-relato/{relatoId}")
    public ResponseEntity<?> vincularRelato(@PathVariable String userId, @PathVariable String relatoId) {
        return userRepository.findById(userId).map(user -> {
            if (!user.getRelatosIds().contains(relatoId)) {
                user.getRelatosIds().add(relatoId);
                userRepository.save(user);
            }
            return ResponseEntity.ok("Relato vinculado ao histórico privado do usuário.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- BUSCAR PERFIL ---
    @GetMapping("/{id}")
    public ResponseEntity<User> buscarPerfil(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}