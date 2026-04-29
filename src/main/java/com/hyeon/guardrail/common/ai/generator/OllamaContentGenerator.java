package com.hyeon.guardrail.common.ai.generator;

import com.hyeon.guardrail.common.ai.config.AiClientConfig.AiProperties;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;
import com.hyeon.guardrail.common.ai.exception.AiClientException;
import com.hyeon.guardrail.common.ai.support.ProductDescriptionPromptFactory;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

/** Ollama 기반 상품 설명 생성 구현체 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OllamaContentGenerator implements AiContentGenerator {

  private final ChatModel chatModel;
  private final AiProperties aiProperties;
  private final ProductDescriptionPromptFactory promptFactory;

  /** Spring AI OllamaChatModel을 사용한 상품 설명 생성 */
  @Override
  public ProductDescriptionGenerateResult generateProductDescription(
      ProductDescriptionGenerateCommand command) {
    validateAvailable();

    String prompt = promptFactory.createPrompt(command);

    try {
      ChatResponse response = chatModel.call(new Prompt(prompt));
      String description = response.getResult().getOutput().getText();
      validateDescription(description);
      return new ProductDescriptionGenerateResult(description);
    } catch (AiClientException exception) {
      throw exception;
    } catch (Exception exception) {
      log.warn("Ollama request failed.");
      throw new AiClientException(
          BaseResponseStatus.INTERNAL_SERVER_ERROR, "Ollama 호출에 실패했습니다.", exception);
    }
  }

  private void validateAvailable() {
    if (!aiProperties.isEnabled()) {
      throw new AiClientException(BaseResponseStatus.INVALID_REQUEST, "AI 기능이 비활성화되어 있습니다.");
    }
  }

  private void validateDescription(String description) {
    if (description == null || description.isBlank()) {
      throw new AiClientException(
          BaseResponseStatus.INTERNAL_SERVER_ERROR, "Ollama 응답에서 설명을 찾을 수 없습니다.");
    }
  }
}
