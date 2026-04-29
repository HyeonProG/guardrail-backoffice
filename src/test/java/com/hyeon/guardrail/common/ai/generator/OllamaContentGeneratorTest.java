package com.hyeon.guardrail.common.ai.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.common.ai.config.AiClientConfig.AiProperties;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;
import com.hyeon.guardrail.common.ai.exception.AiClientException;
import com.hyeon.guardrail.common.ai.support.ProductDescriptionPromptFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

/** Ollama 상품 설명 생성기 단위 테스트 */
class OllamaContentGeneratorTest {

  private static final ProductDescriptionGenerateCommand COMMAND =
      new ProductDescriptionGenerateCommand("린넨 셔츠", "상의", "색상: 화이트", List.of("가벼움"));

  /** AI 기능이 비활성화되면 Ollama 호출 전에 차단 */
  @Test
  void generateProductDescriptionThrowsWhenAiDisabled() {
    AiProperties properties = enabledAiProperties();
    properties.setEnabled(false);
    OllamaContentGenerator generator = generator(properties, mock(ChatModel.class));

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("AI 기능이 비활성화되어 있습니다.");
  }

  /** Ollama 정상 응답의 설명 텍스트를 결과 객체로 변환 */
  @Test
  void generateProductDescriptionReturnsDescriptionTextFromOllamaResponse() {
    AiProperties properties = enabledAiProperties();
    ChatModel chatModel = mock(ChatModel.class);
    ChatResponse response = mock(ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
    when(chatModel.call(any(Prompt.class))).thenReturn(response);
    when(response.getResult().getOutput().getText()).thenReturn("가볍고 시원한 착용감의 린넨 셔츠입니다.");
    OllamaContentGenerator generator = generator(properties, chatModel);

    ProductDescriptionGenerateResult result = generator.generateProductDescription(COMMAND);

    assertThat(result.getDescriptionText()).isEqualTo("가볍고 시원한 착용감의 린넨 셔츠입니다.");
  }

  /** Ollama 빈 응답은 예외로 처리한다 */
  @Test
  void generateProductDescriptionThrowsWhenOllamaResponseBlank() {
    AiProperties properties = enabledAiProperties();
    ChatModel chatModel = mock(ChatModel.class);
    ChatResponse response = mock(ChatResponse.class, Answers.RETURNS_DEEP_STUBS);
    when(chatModel.call(any(Prompt.class))).thenReturn(response);
    when(response.getResult().getOutput().getText()).thenReturn(" ");
    OllamaContentGenerator generator = generator(properties, chatModel);

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("Ollama 응답에서 설명을 찾을 수 없습니다.");
  }

  /** Ollama 호출 실패를 AiClientException으로 감싼다 */
  @Test
  void generateProductDescriptionWrapsOllamaFailure() {
    AiProperties properties = enabledAiProperties();
    ChatModel chatModel = mock(ChatModel.class);
    when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("failed"));
    OllamaContentGenerator generator = generator(properties, chatModel);

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("Ollama 호출에 실패했습니다.");
  }

  private OllamaContentGenerator generator(AiProperties properties, ChatModel chatModel) {
    return new OllamaContentGenerator(chatModel, properties, new ProductDescriptionPromptFactory());
  }

  private AiProperties enabledAiProperties() {
    AiProperties properties = new AiProperties();
    properties.setEnabled(true);
    return properties;
  }
}
