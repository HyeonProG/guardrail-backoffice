package com.hyeon.guardrail.common.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** AI 기능 공통 설정 */
@Configuration
@EnableConfigurationProperties(AiClientConfig.AiProperties.class)
public class AiClientConfig {

  /** app.ai 설정 속성 */
  @Getter
  @Setter
  @ConfigurationProperties(prefix = "app.ai")
  public static class AiProperties {

    private boolean enabled = true;
  }
}
