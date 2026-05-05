package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // Permite integração com o seu front-end
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // --- CADASTRO ---
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody User user) {
        if (userRepository.existsByCpf(user.getCpf())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CPF já cadastrado.");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail já cadastrado.");
        }
        
        // O plano "FREE" já é inicializado na Model
        User novoUser = userRepository.save(user);
        return new ResponseEntity<>(novoUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String cpfEnviado = body.get("login"); // O seu JS envia como 'login'
        String senhaEnviada = body.get("password");

        // Busca o usuário pelo CPF
        Optional<User> userOpt = userRepository.findByCpf(cpfEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Verifica se a senha coincide
            // IMPORTANTE: Se você estiver usando criptografia, use passwordEncoder.matches()
            if (user.getPassword().equals(senhaEnviada)) {
                
                // Retorna dados úteis para o front-end
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("nome", user.getNome());
                response.put("plano", user.getPlano());
                response.put("token", "fake-jwt-token-" + user.getId()); // Token fictício para o JS não quebrar
                
                return ResponseEntity.ok(response);
            }
        }

        // Se chegar aqui, ou o CPF não existe ou a senha está errada
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