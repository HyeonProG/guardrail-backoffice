package com.hyeon.guardrail.common.ai.support;

import com.hyeon.guardrail.common.ai.dto.ProductDescriptionGenerateCommand;
import java.util.List;
import org.springframework.stereotype.Component;

/** 상품 설명 생성 전용 프롬프트 팩토리 */
@Component
public class ProductDescriptionPromptFactory {

  /** 상품 설명 생성 명령을 프롬프트 문자열로 변환 */
  public String createPrompt(ProductDescriptionGenerateCommand command) {
    return """
        아래 상품 정보를 바탕으로 한국어 상품 설명 본문만 작성하세요.

        상품명: %s
        카테고리명: %s
        옵션 요약: %s
        특징 키워드: %s

        작성 규칙:
        - 과장 표현, 허위 성능, 확인되지 않은 효능 표현은 사용하지 마세요.
        - 이미지 URL, 파일 경로, 바이너리 데이터는 사용하지 마세요.
        - 확인된 상품 정보 안에서만 자연스러운 설명을 작성하세요.
        """
        .formatted(
            command.getProductName(),
            command.getCategoryName(),
            command.getOptionSummary(),
            String.join(", ", safeKeywords(command.getFeatureKeywords())));
  }

  private List<String> safeKeywords(List<String> featureKeywords) {
    if (featureKeywords == null) {
      return List.of();
    }
    return featureKeywords;
  }
}
