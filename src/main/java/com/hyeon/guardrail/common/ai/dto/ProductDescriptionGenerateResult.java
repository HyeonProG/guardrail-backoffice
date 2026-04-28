package com.hyeon.guardrail.common.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 상품 설명 생성 결과 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDescriptionGenerateResult {

  private String descriptionText;
}
