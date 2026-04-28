package com.hyeon.guardrail.common.ai.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/** AI provider HTTP client 설정 */
@Configuration
@EnableConfigurationProperties(AiClientConfig.AiProperties.class)
public class AiClientConfig {

  /** AI provider 호출용 RestClient 생성 */
  @Bean
  public RestClient aiRestClient(AiProperties properties) {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
    requestFactory.setConnectTimeout(timeout);
    requestFactory.setReadTimeout(timeout);

    return RestClient.builder()
        .baseUrl(properties.getBaseUrl())
        .requestFactory(requestFactory)
        .build();
  }

  /** app.ai 설정 속성 */
  @Getter
  @Setter
  @ConfigurationProperties(prefix = "app.ai")
  public static class AiProperties {

    private boolean enabled = true;
    private String provider = "openai";
    private String model = "gpt-4.1-mini";
    private long timeoutSeconds = 30;
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey = "";
  }
}
