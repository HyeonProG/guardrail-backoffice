package com.hyeon.guardrail.product.dto;

import com.hyeon.guardrail.product.domain.ProductHistory;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품 처리 이력 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품 처리 이력 응답")
public class ProductHistoryResponse {

  @Schema(description = "상품 이력 ID")
  private UUID id;

  @Schema(description = "상품 ID")
  private UUID productId;

  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;

  @Schema(description = "처리자 이름")
  private String actorName;

  @Schema(description = "상품 이력 유형", example = "APPROVED")
  private ProductHistoryType type;

  @Schema(description = "반려 또는 상태 변경 사유")
  private String reason;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  /** 상품 이력 엔티티를 응답으로 변환 */
  public static ProductHistoryResponse from(ProductHistory productHistory, String actorName) {
    return new ProductHistoryResponse(
        productHistory.getId(),
        productHistory.getProductId(),
        productHistory.getActorId(),
        actorName,
        productHistory.getType(),
        productHistory.getReason(),
        productHistory.getCreatedAt(),
        productHistory.getUpdatedAt());
  }
}
