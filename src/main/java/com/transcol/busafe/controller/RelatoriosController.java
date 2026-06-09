package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.*;
import com.transcol.busafe.config.TokenService;
import com.transcol.busafe.repository.UserRepository;
import com.transcol.busafe.service.RelatorioService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/relatorios")
@CrossOrigin(origins = "*")
public class RelatoriosController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RelatorioService relatorioService;

    // --- VERIFICAR ACESSO A RELATÓRIOS ---
    @GetMapping("/verificar-acesso")
    public ResponseEntity<?> verificarAcesso(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User user = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            // CORREÇÃO: Comparação segura de Enum
            boolean isPessoaFisica = TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario());
            boolean isPlanoFree = Plano.FREE.equals(user.getPlano());
            boolean temAcesso = !(isPessoaFisica && isPlanoFree);

            return ResponseEntity.ok(Map.of("acessoPermitido", temAcesso));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
        }
    }

    // --- GERAR RELATÓRIO DE RELATOS ---
    @GetMapping
    public ResponseEntity<?> gerarRelatorio(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User user = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            // CORREÇÃO: Comparação segura de Enum
            boolean isPessoaFisica = TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario());
            boolean isPlanoFree = Plano.FREE.equals(user.getPlano());
            
            if (isPessoaFisica && isPlanoFree) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Relatórios disponíveis apenas para planos Individual ou Empresarial.");
            }

            Map<String, Object> relatorio = relatorioService.gerarRelatorioRelatos(user);
            return ResponseEntity.ok(relatorio);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao gerar relatório: " + e.getMessage());
        }
    }
}