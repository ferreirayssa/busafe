package com.transcol.busafe.controller;

import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.*;
import com.transcol.busafe.repository.UserRepository;
import com.transcol.busafe.service.HashService;
import com.transcol.busafe.service.RelatorioService;
import com.transcol.busafe.service.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Autowired
    private HashService hashService;
    

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

            boolean isPessoaFisica = TipoUsuario.PESSOA_FISICA.equals(user.getTipoUsuario());
            boolean isPlanoFree = Plano.FREE.equals(user.getPlano());
            boolean temAcesso = !(isPessoaFisica && isPlanoFree);

            return ResponseEntity.ok(Map.of(
                "acessoPermitido", temAcesso,
                "tipoUsuario", user.getTipoUsuario().name(),
                "plano", user.getPlano().name(),
                "nome", user.getNome()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
        }
    }

    // --- GERAR RELATÓRIO COMPLETO DE RELATOS ---
    @GetMapping
    public ResponseEntity<?> gerarRelatorio(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User user = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

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

    // --- ESTATÍSTICAS RÁPIDAS PARA DASHBOARD ---
@GetMapping("/estatisticas")
public ResponseEntity<?> getEstatisticas(@RequestHeader("Authorization") String token) {
    try {
        String jwt = token.replace("Bearer ", "");
        String emailUsuario = tokenService.getSubject(jwt);
        System.out.println("📊 Buscando estatísticas para: " + emailUsuario);

        User user = userRepository.findByEmail(emailUsuario).orElse(null);
        
        if (user == null) {
            System.out.println("❌ Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Usuário não encontrado."));
        }
        
        System.out.println("👤 Usuário: " + user.getNome() + " | Hash: " + hashService.gerarHash(user.getId()));

        Map<String, Object> stats = relatorioService.getEstatisticasDashboard(user);
        System.out.println("✅ Estatísticas: " + stats);
        return ResponseEntity.ok(stats);

    } catch (Exception e) {
        System.err.println("❌ Erro: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("erro", e.getMessage()));
    }
}

    // --- FILTRAR RELATOS POR PERÍODO ---
    @GetMapping("/filtrar")
    public ResponseEntity<?> filtrarRelatos(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String dataInicio,
            @RequestParam(required = false) String dataFim,
            @RequestParam(required = false) String linha,
            @RequestParam(required = false) String tipo) {
        
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User user = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            Map<String, Object> filtros = new HashMap<>();
            
            // Processar datas
            if (dataInicio != null && !dataInicio.isEmpty()) {
                LocalDateTime inicio = LocalDateTime.parse(dataInicio + "T00:00:00");
                filtros.put("dataInicio", inicio);
            }
            if (dataFim != null && !dataFim.isEmpty()) {
                LocalDateTime fim = LocalDateTime.parse(dataFim + "T23:59:59");
                filtros.put("dataFim", fim);
            }
            
            filtros.put("linha", linha);
            filtros.put("tipo", tipo);
            
            Map<String, Object> resultado = relatorioService.filtrarRelatos(user, filtros);  //erro aqui
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao filtrar relatos: " + e.getMessage());
        }
    }

    // --- RELATÓRIO POR TIPO DE OCORRÊNCIA ---
    @GetMapping("/por-tipo/{tipo}")
    public ResponseEntity<?> getRelatosPorTipo(
            @RequestHeader("Authorization") String token,
            @PathVariable String tipo) {
        
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User user = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            List<Map<String, Object>> relatos = relatorioService.getRelatosPorTipo(user, tipo);  //erro aqui
            return ResponseEntity.ok(relatos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar relatos: " + e.getMessage());
        }
    }

    // --- TOP LOCAIS COM MAIS OCORRÊNCIAS ---
    @GetMapping("/top-locais")
    public ResponseEntity<?> getTopLocais(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String emailUsuario = tokenService.getSubject(jwt);

            User user = userRepository.findByEmail(emailUsuario).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado.");
            }

            List<Map<String, Object>> topLocais = relatorioService.getTopLocais(user);  //erro aqui
            return ResponseEntity.ok(topLocais);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar top locais: " + e.getMessage());
        }
    }

    
}