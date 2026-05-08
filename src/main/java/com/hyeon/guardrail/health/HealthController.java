package com.hyeon.guardrail.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** health check */
@RestController
@RequestMapping("/health")
public class HealthController {

  /** health check */
  @GetMapping
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("ok");
  }
}
