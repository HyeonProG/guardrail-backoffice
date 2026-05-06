package com.hyeon.guardrail.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** 공통 응답 직렬화 단위 테스트 */
class BaseResponseEntityTest {

  /** 응답 JSON은 isSuccess를 유지하고 내부 상태 코드는 노출하지 않는다 */
  @Test
  void serializesPublicResponseContractOnly() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    BaseResponseEntity<Map<String, String>> response =
        BaseResponseEntity.success(Map.of("name", "guardrail"), "정상 응답");

    String json = objectMapper.writeValueAsString(response);

    assertThat(json).contains("\"isSuccess\":true");
    assertThat(json).contains("\"message\":\"정상 응답\"");
    assertThat(json).contains("\"code\":200");
    assertThat(json).contains("\"result\":{\"name\":\"guardrail\"}");
    assertThat(json).doesNotContain("httpStatusCode");
    assertThat(json).doesNotContain("\"success\":");
  }
}
