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
        아래 상품 정보를 바탕으로 판매 페이지에 바로 사용할 한국어 상품 설명 본문만 작성하세요.
        사용자가 상품을 구매하고 싶어지도록, 짧은 키워드도 실제 사용 장면과 고객이 얻는 장점으로 자연스럽게 확장하세요.

        상품명: %s
        카테고리명: %s
        옵션 요약: %s
        특징 키워드: %s

        작성 규칙:
        - 출력은 반드시 자연스러운 한국어만 사용하세요.
        - 영어, 태국어, 일본어, 중국어, 외래어 남용, 이모지, 마크다운 문법은 사용하지 마세요.
        - 입력에 없는 성능, 효능, 기능, 소재, 브랜드, 인증 정보는 임의로 추가하지 마세요.
        - 과장 표현, 허위 성능, 확인되지 않은 효능 표현은 사용하지 마세요.
        - 이미지 URL, 파일 경로, 바이너리 데이터는 사용하지 마세요.
        - 확인된 상품 정보 안에서만 구매 전환에 도움이 되도록 자연스럽고 구체적으로 설명하세요.
        - 키워드가 한두 단어뿐이어도 고객 관점의 사용 이유, 사용 상황, 기대할 수 있는 편의성을 연결하세요.
        - 단순 나열 대신 "왜 이 상품을 선택하면 좋은지"가 드러나게 작성하세요.
        - 문장은 3문장 또는 4문장으로 작성하세요.
        - 첫 문장은 상품의 용도와 핵심 특징을 설명하세요.
        - 다음 문장은 색상, 형태, 착용감, 보관, 사용 장면 중 입력 정보와 어울리는 요소를 자연스럽게 설명하세요.
        - 마지막 문장은 부담 없이 선택할 수 있는 구매 설득 문장으로 마무리하세요.
        - 제목, 소제목, 불릿 포인트 없이 본문만 출력하세요.
        - 출력 예시 형식:
          검정색 운동화로 일상과 가벼운 야외 활동에 편하게 신기 좋은 상품입니다.
          깔끔한 색감 덕분에 다양한 옷차림에 자연스럽게 어울립니다.
          매일 신기 좋은 활용도를 찾는 고객에게 부담 없이 추천할 수 있습니다.
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
