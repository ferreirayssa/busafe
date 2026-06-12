package com.transcol.busafe.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transcol.busafe.model.Relato;
import com.transcol.busafe.repository.RelatoRepository;
import com.transcol.busafe.service.HashService;
import com.transcol.busafe.service.TokenService;

@RestController
@RequestMapping("/relatos")
public class RelatoController {

    private final RelatoRepository relatoRepo;
    private final TokenService tokenService;
    private final HashService hashService;

    public RelatoController(RelatoRepository relatoRepo, TokenService tokenService, HashService hashService) {
        this.relatoRepo = relatoRepo;
        this.tokenService = tokenService;
        this.hashService = hashService;
    }

    // Criar um novo relato (POST) - COM HASH PSEUDO-ANÔNIMO
    @PostMapping
    public ResponseEntity<Relato> criarRelato(
            @RequestBody Relato relato,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        // Se tiver token, extrai o usuário e gera hash
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.replace("Bearer ", "");
            
            try {
                if (tokenService.validarToken(token)) {
                    // Extrai o ID do usuário usando o TokenService
                    String usuarioId = tokenService.getUserId(token);
                    
                    // Gera o hash pseudo-anônimo
                    String usuarioHash = hashService.gerarHash(usuarioId);
                    relato.setUsuarioHash(usuarioHash);
                    
                    System.out.println("✅ Relato vinculado ao hash: " + usuarioHash.substring(0, 20) + "...");
                }
            } catch (Exception e) {
                System.err.println("❌ Token inválido: " + e.getMessage());
                // Salva sem hash (anônimo)
            }
        } else {
            System.out.println("⚠️ Relato anônimo (sem token)");
        }
        
        Relato novoRelato = relatoRepo.save(relato);
        return new ResponseEntity<>(novoRelato, HttpStatus.CREATED);
    }

    // Listar todos os relatos (GET) - usuarioHash é WRITE_ONLY, não aparece
    @GetMapping
    public List<Relato> listarTodos() {
        return relatoRepo.findAll();
    }

    // Buscar relatos por linha Transcol (GET)
    @GetMapping("/linha/{numero}")
    public ResponseEntity<List<Relato>> buscarPorLinha(@PathVariable Integer numero) {
        List<Relato> relatos = relatoRepo.findByLinhaTranscol(numero);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }

    // Buscar relatos por linha Municipal
    @GetMapping("/municipal/{numero}")
    public ResponseEntity<List<Relato>> buscarPorLinhaMunicipal(@PathVariable Integer numero) {
        List<Relato> relatos = relatoRepo.findByLinhaMunicipal(numero);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }

    // Buscar relatos por tipo
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Relato>> buscarPorTipo(@PathVariable String tipo) {
        List<Relato> relatos = relatoRepo.findByTipo(tipo);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }

    // Buscar relatos por município
    @GetMapping("/municipio/{municipio}")
    public ResponseEntity<List<Relato>> buscarPorMunicipio(@PathVariable String municipio) {
        List<Relato> relatos = relatoRepo.findByMunicipio(municipio);
        if (relatos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(relatos);
    }
    
    // ⬇️ ENDPOINT ADMIN: Verificar se um hash pertence a um usuário ⬇️
    @GetMapping("/admin/verificar-hash/{usuarioId}")
    public ResponseEntity<?> verificarHash(
            @PathVariable String usuarioId,
            @RequestHeader("Authorization") String authHeader) {
        
        // Verifica se o token é válido
        String token = authHeader.replace("Bearer ", "");
        if (!tokenService.validarToken(token)) {
            return ResponseEntity.status(401).body("Token inválido");
        }
        
        // TODO: Adicionar verificação de role ADMIN
        // String tipoUsuario = tokenService.getTipoUsuario(token);
        // if (!"ADMIN".equals(tipoUsuario)) return ResponseEntity.status(403).body("Acesso negado");
        
        String hash = hashService.gerarHash(usuarioId);
        List<Relato> relatos = relatoRepo.findByUsuarioHash(hash); //erro aqui
        
        return ResponseEntity.ok(new VerificacaoHashResponse(
            usuarioId,
            hash,
            relatos.size() + " relatos encontrados"
        ));
    }
    
    // DTO interno para resposta do endpoint admin
    static class VerificacaoHashResponse {
        public String usuarioId;
        public String hash;
        public String resultado;
        
        public VerificacaoHashResponse(String usuarioId, String hash, String resultado) {
            this.usuarioId = usuarioId;
            this.hash = hash;
            this.resultado = resultado;
        }
    }
}