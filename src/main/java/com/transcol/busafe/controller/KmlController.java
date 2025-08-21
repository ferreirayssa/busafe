package com.transcol.busafe.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/rotas")
public class KmlController {

  @GetMapping(value="/rotas-transcol.kml", produces="application/vnd.google-earth.kml+xml")
  public ResponseEntity<byte[]> kml() throws IOException {
    var res = new ClassPathResource("static/rotas/rotas-transcol.kml");
    byte[] bytes = res.getContentAsByteArray();
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .contentType(MediaType.parseMediaType("application/vnd.google-earth.kml+xml"))
        .body(bytes);
  }
}
