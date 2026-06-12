package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.TipoUsuario;
import com.transcol.busafe.repository.UserRepository;
import com.transcol.busafe.service.TokenService;
import com.transcol.busafe.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vinculados")
public class VinculadosController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Adicione esta injeção

    // --- LISTAR VINCULADOS ---
    @GetMapping
    public ResponseEntity<?> listarVinculados(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User usuarioLogado = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (usuarioLogado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            // Verifica se é PESSOA_JURIDICA
            if (usuarioLogado.getTipoUsuario() != TipoUsuario.PESSOA_JURIDICA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso permitido apenas para Pessoa Jurídica.");
            }

            // 🔥 MUDANÇA 1: Buscar por contaPaiId em vez de CNPJ
            // Busca todos os usuários que têm esta empresa como contaPai
            List<User> vinculados = userRepository.findByContaPaiId(usuarioLogado.getId());

            List<Map<String, Object>> resposta = vinculados.stream().map(v -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", v.getId());
                map.put("nome", v.getNome());
                map.put("email", v.getEmail());
                map.put("cpf", v.getCpf());
                map.put("plano", v.getPlano().name());
                map.put("ativo", v.isAtivo());
                map.put("contaPaiId", v.getContaPaiId()); // Adicionar para debug
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Erro ao carregar vinculados: " + e.getMessage());
        }
    }
        @PostMapping
        public ResponseEntity<?> adicionarVinculado(@RequestHeader("Authorization") String token, 
                                                    @RequestBody Map<String, String> body) {
            try {
                String jwt = token.replace("Bearer ", "");
                String emailUsuario = tokenService.getSubject(jwt);

                User usuarioLogado = userRepository.findByEmail(emailUsuario).orElse(null);
                
                if (usuarioLogado == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
                }

                // Verifica se é PESSOA_JURIDICA
                if (usuarioLogado.getTipoUsuario() != TipoUsuario.PESSOA_JURIDICA) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas Pessoa Jurídica pode adicionar vinculados.");
                }

                // 🔥 OBTEM O contaPaiId do body (vindo do frontend)
                String contaPaiId = body.get("contaPaiId");
                
                // Se não veio do frontend, usa o ID do usuarioLogado
                if (contaPaiId == null || contaPaiId.isBlank()) {
                    contaPaiId = usuarioLogado.getId();
                }
                

                // Cria o novo usuário vinculado
                User novoVinculado = new User();
                novoVinculado.setNome(body.get("nome"));
                novoVinculado.setEmail(body.get("email"));
                novoVinculado.setCpf(body.get("cpf"));

                // 🔥 CRIPTOGRAFAR A SENHA
                String senhaCriptografada = passwordEncoder.encode(body.get("password"));
                novoVinculado.setPassword(senhaCriptografada);

                novoVinculado.setTipoUsuario(TipoUsuario.PESSOA_FISICA);
                novoVinculado.setPlano(usuarioLogado.getPlano()); // Herda o plano da PJ
                novoVinculado.setCnpj(usuarioLogado.getCnpj()); // Vincula ao CNPJ
                novoVinculado.setContaPaiId(contaPaiId); // 🔥 SETA O CONTA_PAI_ID
                novoVinculado.setAtivo(true);

                User salvo = userService.salvarUsuarioVinculado(novoVinculado);
                
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("id", salvo.getId());
                resposta.put("nome", salvo.getNome());
                resposta.put("email", salvo.getEmail());
                resposta.put("cpf", salvo.getCpf());
                resposta.put("mensagem", "Vinculado adicionado com sucesso!");

                return ResponseEntity.status(HttpStatus.CREATED).body(resposta);

            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar vinculado: " + e.getMessage());
            }
        }

    // --- REMOVER VINCULADO ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removerVinculado(@RequestHeader("Authorization") String token, 
                                               @PathVariable String id) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User usuarioLogado = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (usuarioLogado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            if (usuarioLogado.getTipoUsuario() != TipoUsuario.PESSOA_JURIDICA) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado.");
            }

             User vinculado = userRepository.findById(id).orElse(null);
            
            if (vinculado == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vinculado não encontrado.");
            }

            // Verifica se o vinculado pertence à mesma empresa
            if (!usuarioLogado.getCnpj().equals(vinculado.getCnpj())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Este usuário não pertence à sua empresa.");
            }

            // Soft delete (desativa)
            vinculado.setAtivo(false);
            userRepository.save(vinculado);

            return ResponseEntity.ok("Vinculado removido com sucesso.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao remover vinculado: " + e.getMessage());
        }
    }
}