package com.hyeon.guardrail.productcontent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 설명 AI 초안 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 설명 AI 초안 생성 요청")
public class ProductContentGenerateRequest {

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;

  @NotBlank
  @Schema(description = "상품명", example = "가드레일 티셔츠")
  private String productName;

  @NotBlank
  @Schema(description = "카테고리명", example = "상의")
  private String categoryName;

  @NotBlank
  @Schema(description = "옵션 요약", example = "색상: 블랙, 화이트 / 사이즈: M, L")
  private String optionSummary;

  @NotEmpty
  @Schema(description = "특징 키워드", example = "[\"면 100%\", \"편안한 착용감\"]")
  private List<String> featureKeywords;
}
