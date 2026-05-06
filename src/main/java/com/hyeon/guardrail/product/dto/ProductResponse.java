package com.hyeon.guardrail.product.dto;

import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품 기본 정보 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품 기본 정보 응답")
public class ProductResponse {

  @Schema(description = "상품 ID")
  private UUID id;

  @Schema(description = "카테고리 ID")
  private UUID categoryId;

  @Schema(description = "상품명", example = "가드레일 티셔츠")
  private String name;

  @Schema(description = "상품 설명")
  private String description;

  @Schema(description = "선택된 옵션값 목록")
  private List<ProductSelectedOptionResponse> selectedOptions;

  @Schema(description = "상품 상태", example = "DRAFT")
  private ProductStatus status;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  /** 상품 엔티티를 응답으로 변환 */
  public static ProductResponse from(
      Product product, List<ProductSelectedOptionResponse> selectedOptions) {
    return new ProductResponse(
        product.getId(),
        product.getCategoryId(),
        product.getName(),
        product.getDescription(),
        selectedOptions,
        product.getStatus(),
        product.getCreatedAt(),
        product.getUpdatedAt());
  }
}
