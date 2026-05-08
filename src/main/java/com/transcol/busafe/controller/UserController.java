package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.config.TokenService; // O import está certo!
import com.transcol.busafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private BCryptPasswordEncoder passwordEncoder;

    // ESTA LINHA ABAIXO É A QUE ESTAVA FALTANDO:
    @Autowired
    private TokenService tokenService; 

    // --- CADASTRO ---
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody User user) {
        // ... (seu código de registro permanece igual)
        try {
            if (userRepository.existsByCpf(user.getCpf())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CPF já cadastrado.");
            }
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("E-mail já cadastrado.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User novoUser = userRepository.save(user);
            return new ResponseEntity<>(novoUser, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String cpfEnviado = body.get("login"); 
        String senhaEnviada = body.get("password");

        Optional<User> userOpt = userRepository.findByCpf(cpfEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (passwordEncoder.matches(senhaEnviada, user.getPassword())) {

                // Agora o Java vai reconhecer o tokenService abaixo:
                String tokenReal = tokenService.gerarToken(user.getCpf());
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("nome", user.getNome());
                response.put("plano", user.getPlano());
                response.put("token", tokenReal);
                response.put("rotasFavoritas", user.getRotasFavoritas());
                
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("CPF ou senha incorretos.");
    }

    // --- FAVORITAR ROTA ---
    @PostMapping("/{id}/rotas-fav")
    public ResponseEntity<?> favoritarRota(@PathVariable String id, @RequestBody Map<String, String> body) {
        String codigoRota = body.get("codigo"); // Ex: "505"
        
        return userRepository.findById(id).map(user -> {
            // Verifica se a rota já não está nos favoritos para não duplicar
            if (!user.getRotasFavoritas().contains(codigoRota)) {
                user.getRotasFavoritas().add(codigoRota);
                userRepository.save(user);
                return ResponseEntity.ok(user.getRotasFavoritas()); // Retorna a lista atualizada
            }
            return ResponseEntity.badRequest().body("Rota já está nos favoritos.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- REMOVER ROTA FAVORITA ---
    @DeleteMapping("/{id}/remover-rota/{codigo}")
    public ResponseEntity<?> removerRota(@PathVariable String id, @PathVariable String codigo) {
        return userRepository.findById(id).map(user -> {
            if (user.getRotasFavoritas().contains(codigo)) {
                user.getRotasFavoritas().remove(codigo);
                userRepository.save(user);
                return ResponseEntity.ok(user.getRotasFavoritas());
            }
            return ResponseEntity.badRequest().body("Rota não encontrada nos favoritos.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- BUSCAR APENAS FAVORITOS ---
    @GetMapping("/{id}/rotas-fav")
    public ResponseEntity<?> buscarFavoritos(@PathVariable String id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user.getRotasFavoritas()))
                .orElse(ResponseEntity.notFound().build());
    }

}