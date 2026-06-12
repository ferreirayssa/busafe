package com.transcol.busafe.controller;

import com.transcol.busafe.model.Rota;
import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.TipoUsuario;
import com.transcol.busafe.repository.RotaRepository;
import com.transcol.busafe.repository.UserRepository;
import com.transcol.busafe.service.TokenService;
import com.transcol.busafe.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
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

    // --- CONTAGEM DE USUÁRIOS ATIVOS (público, usado na landing page) ---
    @GetMapping("/count")
    public ResponseEntity<Long> contarUsuariosAtivos() {
        return ResponseEntity.ok(userService.allUsers());
    }

    // --- CADASTRO ---
    @PostMapping("/register")
    public ResponseEntity<?> registrar(@RequestBody User user) {
        try {
            // Agora delegamos TUDO para o UserService, que vai aplicar as regras de negócio (PF/PJ/Contas Vinculadas)
            User novoUser = userService.salvarUsuario(user);
            return new ResponseEntity<>(novoUser, HttpStatus.CREATED);
            
        } catch (IllegalArgumentException e) {
            // Captura os erros de negócio (CPF duplicado, CNPJ vazio, erro de subconta, etc)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro: " + e.getMessage());
        }
    }

    // --- LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String loginEnviado = body.get("login");
        String senhaEnviada = body.get("password");

        Optional<User> userOpt = userRepository.findByEmailOrCpfOrCnpj(loginEnviado, loginEnviado, loginEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(senhaEnviada, user.getPassword())) {
                
                String tokenReal = tokenService.gerarToken(user);
                
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("nome", user.getNome());
                response.put("email", user.getEmail());
                response.put("tipoUsuario", user.getTipoUsuario().name());
                response.put("plano", user.getPlano().name());
                response.put("token", tokenReal);
                response.put("rotasFavoritas", user.getRotasFavoritas());
                
                if (user.getTipoUsuario() == TipoUsuario.PESSOA_FISICA) {
                    response.put("cpf", user.getCpf());
                } else {
                    response.put("cnpj", user.getCnpj());
                }
                
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login ou senha incorretos.");
    }

    // --- FAVORITAR ROTA (Via Token) ---
    @PostMapping("/rotas-fav")
    public ResponseEntity<?> favoritarRota(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> body) {
        String codigoEnviado = body.get("codigo");
        String jwt = token.replace("Bearer ", "");
        
        // Agora o subject é o e-mail
        String emailUsuario = tokenService.getSubject(jwt);

        User user = userRepository.findByEmail(emailUsuario).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");

        Rota rotaParaSalvar = rotaRepository.findFirstByLinhaTranscolOrderByIdAsc(codigoEnviado);
        if (rotaParaSalvar == null) {
            rotaParaSalvar = rotaRepository.findFirstByLinhaMunicipalOrderByIdAsc(codigoEnviado);
        }

        if (rotaParaSalvar != null) {
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

    // --- BUSCAR FAVORITOS ---
    @GetMapping("/rotas-fav")
    public ResponseEntity<?> buscarFavoritos(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            return userRepository.findByEmail(emailUsuario)
                    .map(user -> ResponseEntity.ok(user.getRotasFavoritas()))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessão inválida.");
        }
    }

    // --- REMOVER FAVORITOS ---
    @DeleteMapping("/rotas-fav/{codigo}")
    public ResponseEntity<?> removerRota(@RequestHeader("Authorization") String token, @PathVariable String codigo) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            return userRepository.findByEmail(emailUsuario).map(user -> {
                boolean removido = user.getRotasFavoritas().removeIf(r -> 
                    codigo.equals(r.getLinhaTranscol()) || codigo.equals(r.getLinhaMunicipal())
                );

                if (removido) {
                    userRepository.save(user);
                    return ResponseEntity.ok(user.getRotasFavoritas());
                }
                return ResponseEntity.badRequest().body("Rota não encontrada nos favoritos.");
            }).orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sessão inválida.");
        }
    }

    // --- VERIFICAR USUÁRIO ---
    @PostMapping("/verify")
    public ResponseEntity<?> verificarUsuario(@RequestBody Map<String, String> body) {
        String loginEnviado = body.get("documento"); // Pode ser CPF ou CNPJ
        String emailEnviado = body.get("email");

        Optional<User> userOpt = userRepository.findByEmailOrCpfOrCnpj(emailEnviado, loginEnviado, loginEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(emailEnviado)) {
                return ResponseEntity.ok("Usuário validado com sucesso.");
            }
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Dados incorretos.");
    }

    // --- REDEFINIR SENHA ---
    @PutMapping("/reset-password")
    public ResponseEntity<?> redefinirSenha(@RequestBody Map<String, String> body) {
        String loginEnviado = body.get("documento");
        String emailEnviado = body.get("email");
        String novaSenha = body.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmailOrCpfOrCnpj(emailEnviado, loginEnviado, loginEnviado);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(novaSenha));
            userRepository.save(user);
            return ResponseEntity.ok("Senha alterada com sucesso.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
    }
}