package com.hyeon.guardrail.product.dto;

import com.hyeon.guardrail.product.domain.ProductSelectedOption;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품에 선택된 옵션값 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품에 선택된 옵션값 응답")
public class ProductSelectedOptionResponse {

  @Schema(description = "옵션 그룹 ID")
  private UUID productOptionId;

  @Schema(description = "옵션 그룹명")
  private String productOptionName;

  @Schema(description = "옵션값 ID")
  private UUID productOptionItemId;

  @Schema(description = "옵션값명")
  private String productOptionItemName;

  @Schema(description = "추가 금액")
  private int additionalPrice;

  @Schema(description = "정렬 순서")
  private int sortOrder;

  /** 선택 옵션 엔티티를 응답으로 변환 */
  public static ProductSelectedOptionResponse from(ProductSelectedOption productSelectedOption) {
    return new ProductSelectedOptionResponse(
        productSelectedOption.getProductOptionId(),
        productSelectedOption.getProductOptionName(),
        productSelectedOption.getProductOptionItemId(),
        productSelectedOption.getProductOptionItemName(),
        productSelectedOption.getAdditionalPrice(),
        productSelectedOption.getSortOrder());
  }
}
