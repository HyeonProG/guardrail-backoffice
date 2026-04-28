package com.hyeon.guardrail.productoption.dto;

import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품 옵션값 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품 옵션값 응답")
public class ProductOptionItemResponse {

  @Schema(description = "옵션값 ID")
  private UUID id;

  @Schema(description = "옵션 그룹 ID")
  private UUID productOptionId;

  @Schema(description = "옵션값명", example = "블랙")
  private String name;

  @Schema(description = "추가 금액", example = "1000")
  private int additionalPrice;

  @Schema(description = "옵션값 정렬 순서", example = "1")
  private int sortOrder;

  @Schema(description = "옵션값 상태", example = "ACTIVE")
  private ProductOptionStatus status;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  /** 옵션값 엔티티를 응답으로 변환 */
  public static ProductOptionItemResponse from(ProductOptionItem productOptionItem) {
    return new ProductOptionItemResponse(
        productOptionItem.getId(),
        productOptionItem.getProductOptionId(),
        productOptionItem.getName(),
        productOptionItem.getAdditionalPrice(),
        productOptionItem.getSortOrder(),
        productOptionItem.getStatus(),
        productOptionItem.getCreatedAt(),
        productOptionItem.getUpdatedAt());
  }
}
