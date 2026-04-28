package com.hyeon.guardrail.common.ai.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyeon.guardrail.common.ai.config.AiClientConfig.AiProperties;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;
import com.hyeon.guardrail.common.ai.exception.AiClientException;
import com.hyeon.guardrail.common.ai.support.ProductDescriptionPromptFactory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** OpenAI 상품 설명 생성기 단위 테스트 */
class OpenAiContentGeneratorTest {

  private static final ProductDescriptionGenerateCommand COMMAND =
      new ProductDescriptionGenerateCommand("린넨 셔츠", "상의", "색상: 화이트", List.of("가벼움"));

  /** AI 기능이 비활성화되면 provider 호출 전에 차단 */
  @Test
  void generateProductDescriptionThrowsWhenAiDisabled() {
    AiProperties properties = enabledOpenAiProperties();
    properties.setEnabled(false);
    OpenAiContentGenerator generator = generator(properties, RestClient.builder().build());

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("AI 기능이 비활성화되어 있습니다.");
  }

  /** openai가 아닌 provider는 명확한 예외로 실패 */
  @Test
  void generateProductDescriptionThrowsWhenProviderUnsupported() {
    AiProperties properties = enabledOpenAiProperties();
    properties.setProvider("other");
    OpenAiContentGenerator generator = generator(properties, RestClient.builder().build());

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("지원하지 않는 AI provider입니다.");
  }

  /** API 키가 비어 있으면 인증 헤더 생성 전에 실패 */
  @Test
  void generateProductDescriptionThrowsWhenApiKeyBlank() {
    AiProperties properties = enabledOpenAiProperties();
    properties.setApiKey("");
    OpenAiContentGenerator generator = generator(properties, RestClient.builder().build());

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("AI provider API 키가 설정되지 않았습니다.");
  }

  /** OpenAI 정상 응답의 설명 텍스트를 결과 객체로 변환 */
  @Test
  void generateProductDescriptionReturnsDescriptionTextFromOpenAiResponse() {
    AiProperties properties = enabledOpenAiProperties();
    RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    RestClient restClient = builder.build();
    OpenAiContentGenerator generator = generator(properties, restClient);

    server
        .expect(once(), requestTo(properties.getBaseUrl() + "/chat/completions"))
        .andExpect(header("Authorization", "Bearer test-api-key"))
        .andRespond(
            withSuccess(
                """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "가볍고 시원한 착용감의 린넨 셔츠입니다."
                      }
                    }
                  ]
                }
                """,
                MediaType.APPLICATION_JSON));

    ProductDescriptionGenerateResult result = generator.generateProductDescription(COMMAND);

    assertThat(result.getDescriptionText()).isEqualTo("가볍고 시원한 착용감의 린넨 셔츠입니다.");
    server.verify();
  }

  /** OpenAI 응답 실패를 AiClientException으로 감싼다 */
  @Test
  void generateProductDescriptionWrapsOpenAiFailure() {
    AiProperties properties = enabledOpenAiProperties();
    RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    RestClient restClient = builder.build();
    OpenAiContentGenerator generator = generator(properties, restClient);

    server
        .expect(once(), requestTo(properties.getBaseUrl() + "/chat/completions"))
        .andExpect(header("Authorization", "Bearer test-api-key"))
        .andRespond(withServerError());

    assertThatThrownBy(() -> generator.generateProductDescription(COMMAND))
        .isInstanceOf(AiClientException.class)
        .hasMessage("AI provider 호출에 실패했습니다.");
    server.verify();
  }

  private OpenAiContentGenerator generator(AiProperties properties, RestClient restClient) {
    return new OpenAiContentGenerator(
        restClient, new ObjectMapper(), properties, new ProductDescriptionPromptFactory());
  }

  private AiProperties enabledOpenAiProperties() {
    AiProperties properties = new AiProperties();
    properties.setEnabled(true);
    properties.setProvider("openai");
    properties.setModel("gpt-4.1-mini");
    properties.setTimeoutSeconds(30);
    properties.setBaseUrl("https://api.openai.test/v1");
    properties.setApiKey("test-api-key");
    return properties;
  }
}
