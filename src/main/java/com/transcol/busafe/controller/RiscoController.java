package com.transcol.busafe.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;

@RestController
public class RiscoController {
  private final Random rnd = new Random();

  @GetMapping("/risco")
  public Map<String,Object> risco(@RequestParam String linha) {

    double score = Math.min(1.0, Math.max(0.0, rnd.nextDouble()));
    return Map.of("linha", linha, "score", score);
  }
}
