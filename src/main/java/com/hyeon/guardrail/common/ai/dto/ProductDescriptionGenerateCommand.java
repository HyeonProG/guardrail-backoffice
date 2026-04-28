package com.hyeon.guardrail.common.ai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** AI 상품 설명 생성 명령 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDescriptionGenerateCommand {

  private String productName;
  private String categoryName;
  private String optionSummary;
  private List<String> featureKeywords;
}
