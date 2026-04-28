package com.hyeon.guardrail.common.ai.generator;

import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateResult;

/** 상품 설명 생성 AI 공통 인터페이스 */
public interface AiContentGenerator {

  /** 상품 설명 생성 */
  ProductDescriptionGenerateResult generateProductDescription(
      ProductDescriptionGenerateCommand command);
}
