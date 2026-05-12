package com.hyeon.guardrail.common.ai.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 상품 설명 프롬프트 팩토리 단위 테스트 */
class ProductDescriptionPromptFactoryTest {

  private final ProductDescriptionPromptFactory promptFactory =
      new ProductDescriptionPromptFactory();

  /** 상품 설명 생성 입력값과 금지 문구를 프롬프트에 포함 */
  @Test
  void createPromptIncludesProductDescriptionInputsAndRestrictionPolicy() {
    ProductDescriptionGenerateCommand command =
        new ProductDescriptionGenerateCommand("린넨 셔츠", "상의", "색상: 화이트, 네이비", List.of("시원함", "가벼움"));

    String prompt = promptFactory.createPrompt(command);

    assertThat(prompt)
        .contains("린넨 셔츠")
        .contains("상의")
        .contains("색상: 화이트, 네이비")
        .contains("시원함, 가벼움")
        .contains("구매하고 싶어지도록")
        .contains("짧은 키워드")
        .contains("고객 관점의 사용 이유")
        .contains("구매 설득 문장")
        .contains("과장 표현")
        .contains("허위 성능")
        .contains("확인되지 않은 효능 표현");
  }
}
