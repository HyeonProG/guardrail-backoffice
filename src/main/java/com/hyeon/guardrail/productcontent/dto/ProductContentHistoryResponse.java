package com.hyeon.guardrail.productcontent.dto;

import com.hyeon.guardrail.productcontent.domain.ProductContentHistory;
import com.hyeon.guardrail.productcontent.domain.ProductContentHistoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품 설명 처리 이력 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품 설명 처리 이력 응답")
public class ProductContentHistoryResponse {

  @Schema(description = "상품 설명 이력 ID")
  private UUID id;

  @Schema(description = "상품 설명 초안 ID")
  private UUID draftId;

  @Schema(description = "상품 ID")
  private UUID productId;

  @Schema(description = "처리자 사용자 ID")
  private UUID actorId;

  @Schema(description = "처리 이력 유형", example = "GENERATED")
  private ProductContentHistoryType type;

  @Schema(description = "처리 사유")
  private String reason;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  /** 상품 설명 이력 엔티티를 응답으로 변환 */
  public static ProductContentHistoryResponse from(ProductContentHistory history) {
    return new ProductContentHistoryResponse(
        history.getId(),
        history.getDraftId(),
        history.getProductId(),
        history.getActorId(),
        history.getType(),
        history.getReason(),
        history.getCreatedAt());
  }
}
