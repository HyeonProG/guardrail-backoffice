package com.hyeon.guardrail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Guardrail Spring Boot 애플리케이션 진입점 */
@SpringBootApplication
public class GuardrailApplication {

  /** Spring Boot 애플리케이션 실행 */
  public static void main(String[] args) {
    SpringApplication.run(GuardrailApplication.class, args);
  }
}
