package com.hyeon.guardrail.productcontent.dto;

import com.hyeon.guardrail.productcontent.domain.ProductContentDraft;
import com.hyeon.guardrail.productcontent.domain.ProductContentSource;
import com.hyeon.guardrail.productcontent.domain.ProductContentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 상품 설명 초안 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "상품 설명 초안 응답")
public class ProductContentDraftResponse {

  @Schema(description = "상품 설명 초안 ID")
  private UUID id;

  @Schema(description = "상품 ID")
  private UUID productId;

  @Schema(description = "상품 설명 초안 본문")
  private String content;

  @Schema(description = "설명 생성 출처", example = "AI")
  private ProductContentSource source;

  @Schema(description = "설명 초안 상태", example = "GENERATED")
  private ProductContentStatus status;

  @Schema(description = "생성 요청자 ID")
  private UUID requestedByActorId;

  @Schema(description = "검수 제출자 ID")
  private UUID submittedByActorId;

  @Schema(description = "최종 승인자 또는 반려자 ID")
  private UUID approvedByActorId;

  @Schema(description = "반려 사유")
  private String rejectReason;

  @Schema(description = "생성일시")
  private LocalDateTime createdAt;

  @Schema(description = "수정일시")
  private LocalDateTime updatedAt;

  /** 상품 설명 초안 엔티티를 응답으로 변환 */
  public static ProductContentDraftResponse from(ProductContentDraft draft) {
    return new ProductContentDraftResponse(
        draft.getId(),
        draft.getProductId(),
        draft.getContent(),
        draft.getSource(),
        draft.getStatus(),
        draft.getRequestedByActorId(),
        draft.getSubmittedByActorId(),
        draft.getApprovedByActorId(),
        draft.getRejectReason(),
        draft.getCreatedAt(),
        draft.getUpdatedAt());
  }
}
