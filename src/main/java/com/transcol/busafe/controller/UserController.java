package com.transcol.busafe.controller;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.model.User;
import com.transcol.busafe.config.TokenService;
import com.transcol.busafe.repository.RotaRepository;
import com.transcol.busafe.repository.UserRepository;
import com.transcol.busafe.service.UserService;

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
    
    @Autowired
    private RotaRepository rotaRepository;

    @Autowired
    private UserService userService;

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
        String codigoEnviado = body.get("codigo");
        String jwt = token.replace("Bearer ", "");
        String cpf = tokenService.getSubject(jwt);

        User user = userRepository.findByCpf(cpf).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");

        // Busca a rota completa na coleção 'rota'
        Rota rotaParaSalvar = rotaRepository.findFirstByLinhaTranscolOrderByIdAsc(codigoEnviado);
        if (rotaParaSalvar == null) {
            rotaParaSalvar = rotaRepository.findFirstByLinhaMunicipalOrderByIdAsc(codigoEnviado);
        }

        if (rotaParaSalvar != null) {
            // Verifica se já existe para não duplicar (compara pelo número da linha)
            boolean jaExiste = user.getRotasFavoritas().stream()
                    .anyMatch(r -> codigoEnviado.equals(r.getLinhaTranscol()) || codigoEnviado.equals(r.getLinhaMunicipal()));

            if (!jaExiste) {
                user.getRotasFavoritas().add(rotaParaSalvar); 
                userRepository.save(user);
                return ResponseEntity.ok("Linha " + codigoEnviado + " favoritada!");
            }
            return ResponseEntity.badRequest().body("Esta linha já está nos seus favoritos.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Linha não encontrada no sistema.");
    }

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

    @PostMapping("/verify")
    public ResponseEntity<?> verificarUsuario(@RequestBody Map<String, String> body) {
        String cpfEnviado = body.get("cpf");
        String emailEnviado = body.get("email");

        Optional<User> userOpt = userRepository.findByCpf(cpfEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(emailEnviado)) {
                return ResponseEntity.ok("Usuário validado com sucesso.");
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("CPF ou E-mail incorretos.");
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> redefinirSenha(@RequestBody Map<String, String> body) {
        String cpfEnviado = body.get("cpf");
        String novaSenha = body.get("newPassword");

        Optional<User> userOpt = userRepository.findByCpf(cpfEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            user.setPassword(passwordEncoder.encode(novaSenha));
            
            userRepository.save(user);
            
            return ResponseEntity.ok("Senha alterada com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
    }
}