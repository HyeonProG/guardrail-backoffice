package com.hyeon.guardrail.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 설명 AI 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 설명 AI 생성 요청")
public class ProductDescriptionGenerateRequest {

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;

  @Schema(description = "상품명", example = "가드레일 티셔츠")
  private String productName;

  @Schema(description = "카테고리명", example = "상의")
  private String categoryName;

  @Schema(description = "선택 옵션 요약", example = "색상: 블랙 / 사이즈: M")
  private String optionSummary;

  @Schema(description = "설명 생성을 위한 키워드 목록")
  private List<String> featureKeywords;
}
