package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.config.TokenService;
import com.transcol.busafe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService; 

    // --- CADASTRO ---
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody User user) {
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

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String cpfEnviado = body.get("login"); 
        String senhaEnviada = body.get("password");

        Optional<User> userOpt = userRepository.findByCpf(cpfEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(senhaEnviada, user.getPassword())) {
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

    // --- FAVORITAR ROTA (Via Token) ---
    @PostMapping("/rotas-fav")
    public ResponseEntity<?> favoritarRota(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> body) {
        try {
            String jwt = token.replace("Bearer ", "");
            String cpfUsuario = tokenService.getSubject(jwt); 

            return userRepository.findByCpf(cpfUsuario).map(user -> {
                String codigoRota = body.get("codigo");
                if (!user.getRotasFavoritas().contains(codigoRota)) {
                    user.getRotasFavoritas().add(codigoRota);
                    userRepository.save(user);
                    return ResponseEntity.ok("Rota " + codigoRota + " favoritada!");
                }
                return ResponseEntity.badRequest().body("Rota já favoritada.");
            }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessão inválida.");
        }
    }

    // --- BUSCAR APENAS FAVORITOS (Via Token) ---
    @GetMapping("/rotas-fav")
    public ResponseEntity<?> buscarFavoritos(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String cpfUsuario = tokenService.getSubject(jwt);

            return userRepository.findByCpf(cpfUsuario)
                    .map(user -> ResponseEntity.ok(user.getRotasFavoritas()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessão inválida.");
        }
    }

    // --- REMOVER ROTA FAVORITA (Via Token) ---
    @DeleteMapping("/rotas-fav/{codigo}")
    public ResponseEntity<?> removerRota(@RequestHeader("Authorization") String token, @PathVariable String codigo) {
        try {
            String jwt = token.replace("Bearer ", "");
            String cpfUsuario = tokenService.getSubject(jwt);

            return userRepository.findByCpf(cpfUsuario).map(user -> {
                if (user.getRotasFavoritas().contains(codigo)) {
                    user.getRotasFavoritas().remove(codigo);
                    userRepository.save(user);
                    return ResponseEntity.ok(user.getRotasFavoritas());
                }
                return ResponseEntity.badRequest().body("Rota não encontrada nos favoritos.");
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessão inválida.");
        }
    }
}