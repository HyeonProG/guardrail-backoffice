package com.hyeon.guardrail.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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

  public ProductUpdateRequest(UUID categoryId, String name, String description, UUID actorId) {
    this.categoryId = categoryId;
    this.name = name;
    this.description = description;
    this.selectedOptionItemIds = null;
    this.actorId = actorId;
  }

  @NotNull
  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @Schema(description = "상품명", example = "가드레일 티셔츠")
  private String name;

  @Schema(description = "상품 설명", example = "수정된 상품 설명")
  private String description;

  @Schema(description = "선택한 옵션값 ID 목록")
  private List<UUID> selectedOptionItemIds;

  @NotNull
  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;
}
