package com.transcol.busafe.controller;

import com.transcol.busafe.service.ImportKmlService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ImportKmlService importService;

    @Autowired
    public ImportController(ImportKmlService importService) {
        this.importService = importService;
    }

    @PostMapping
    public ResponseEntity<?> importKml(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Arquivo KML não enviado.");
            }
            try (InputStream kmlStream = file.getInputStream()) {
                int count = importService.importar(kmlStream);
                return ResponseEntity.ok("Importação bem-sucedida! " + count + " rotas importadas.");
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Erro ao processar o arquivo: " + e.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao importar o KML: " + e.getMessage());
        }
    }
}