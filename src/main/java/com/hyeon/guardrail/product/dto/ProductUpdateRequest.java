package com.hyeon.guardrail.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 상품 기본 정보 수정 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 기본 정보 수정 요청")
public class ProductUpdateRequest {

  @NotNull
  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @NotBlank
  @Schema(description = "상품명", example = "가드레일 티셔츠")
  private String name;

  @NotBlank
  @Schema(description = "상품 설명", example = "수정된 상품 설명")
  private String description;

  @Min(0)
  @Schema(description = "판매 가능 수량", example = "100")
  private int quantity;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
