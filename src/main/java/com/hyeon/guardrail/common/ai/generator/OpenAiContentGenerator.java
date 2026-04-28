package com.hyeon.guardrail.common.ai.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeon.guardrail.common.ai.config.AiClientConfig.AiProperties;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;
import com.hyeon.guardrail.common.ai.exception.AiClientException;
import com.hyeon.guardrail.common.ai.support.ProductDescriptionPromptFactory;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** OpenAI 기반 상품 설명 생성 구현체 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiContentGenerator implements AiContentGenerator {

  private static final String OPENAI_PROVIDER = "openai";
  private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";

  private final RestClient aiRestClient;
  private final ObjectMapper objectMapper;
  private final AiProperties aiProperties;
  private final ProductDescriptionPromptFactory promptFactory;

  /** OpenAI Chat Completions API를 사용한 상품 설명 생성 */
  @Override
  public ProductDescriptionGenerateResult generateProductDescription(
      ProductDescriptionGenerateCommand command) {
    validateAvailable();

    String prompt = promptFactory.createPrompt(command);
    Map<String, Object> requestBody =
        Map.of(
            "model",
            aiProperties.getModel(),
            "messages",
            List.of(
                Map.of("role", "system", "content", "한국어 상품 설명 초안을 작성하는 보조자입니다."),
                Map.of("role", "user", "content", prompt)));

    try {
      String responseBody =
          aiRestClient
              .post()
              .uri(CHAT_COMPLETIONS_PATH)
              .header("Authorization", "Bearer " + aiProperties.getApiKey())
              .body(requestBody)
              .retrieve()
              .body(String.class);

      return new ProductDescriptionGenerateResult(extractDescription(responseBody));
    } catch (RestClientResponseException exception) {
      log.warn("AI provider request failed. status={}", exception.getStatusCode().value());
      throw new AiClientException(
          BaseResponseStatus.INTERNAL_SERVER_ERROR, "AI provider 호출에 실패했습니다.", exception);
    } catch (ResourceAccessException exception) {
      log.warn("AI provider request timed out or was not reachable.");
      throw new AiClientException(
          BaseResponseStatus.INTERNAL_SERVER_ERROR, "AI provider 호출 시간이 초과되었습니다.", exception);
    } catch (RestClientException exception) {
      log.warn("AI provider request failed.");
      throw new AiClientException(
          BaseResponseStatus.INTERNAL_SERVER_ERROR, "AI provider 호출에 실패했습니다.", exception);
    }
  }

  private void validateAvailable() {
    if (!aiProperties.isEnabled()) {
      throw new AiClientException(BaseResponseStatus.INVALID_REQUEST, "AI 기능이 비활성화되어 있습니다.");
    }
    if (!OPENAI_PROVIDER.equalsIgnoreCase(aiProperties.getProvider())) {
      throw new AiClientException(BaseResponseStatus.INVALID_REQUEST, "지원하지 않는 AI provider입니다.");
    }
    if (aiProperties.getApiKey() == null || aiProperties.getApiKey().isBlank()) {
      throw new AiClientException(
          BaseResponseStatus.INVALID_REQUEST, "AI provider API 키가 설정되지 않았습니다.");
    }
  }

  private String extractDescription(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode content = root.path("choices").path(0).path("message").path("content");
      if (content.isMissingNode() || content.asText().isBlank()) {
        throw new AiClientException(
            BaseResponseStatus.INTERNAL_SERVER_ERROR, "AI provider 응답에서 설명을 찾을 수 없습니다.");
      }
      return content.asText();
    } catch (AiClientException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new AiClientException(
          BaseResponseStatus.INTERNAL_SERVER_ERROR, "AI provider 응답 파싱에 실패했습니다.", exception);
    }
  }
}
