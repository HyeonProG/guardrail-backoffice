package com.hyeon.guardrail.productoption.dto;

import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품 옵션 그룹 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품 옵션 그룹 응답")
public class ProductOptionResponse {

  @Schema(description = "옵션 그룹 ID")
  private UUID id;

  @Schema(description = "상품 ID")
  private UUID productId;

  @Schema(description = "옵션명", example = "색상")
  private String name;

  @Schema(description = "옵션 그룹 정렬 순서", example = "1")
  private int sortOrder;

  @Schema(description = "옵션 그룹 상태", example = "ACTIVE")
  private ProductOptionStatus status;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  @Schema(description = "옵션값 목록")
  private List<ProductOptionItemResponse> items;

  /** 옵션 그룹 엔티티를 응답으로 변환 */
  public static ProductOptionResponse from(ProductOption productOption) {
    return from(productOption, List.of());
  }

  /** 옵션 그룹 엔티티와 옵션값 목록을 응답으로 변환 */
  public static ProductOptionResponse from(
      ProductOption productOption, List<ProductOptionItemResponse> items) {
    return new ProductOptionResponse(
        productOption.getId(),
        productOption.getProductId(),
        productOption.getName(),
        productOption.getSortOrder(),
        productOption.getStatus(),
        productOption.getCreatedAt(),
        productOption.getUpdatedAt(),
        items);
  }
}
