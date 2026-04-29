package com.hyeon.guardrail.productcontent.domain;

import com.hyeon.guardrail.common.domain.SoftDeleteEntity;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 상품 설명 생성 및 검수 초안 엔티티 */
@Getter
@Entity
@Table(name = "product_content_drafts")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductContentDraft extends SoftDeleteEntity {

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "content", nullable = false, length = 2000)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "source", nullable = false)
  private ProductContentSource source;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ProductContentStatus status;

  @Column(name = "requested_by_actor_id", nullable = false)
  private UUID requestedByActorId;

  @Column(name = "submitted_by_actor_id")
  private UUID submittedByActorId;

  @Column(name = "approved_by_actor_id")
  private UUID approvedByActorId;

  @Column(name = "reject_reason", length = 1000)
  private String rejectReason;

  /** 상품 설명 초안 본문을 수정 */
  public void updateContent(String content) {
    this.content = content;
  }

  /** 생성 또는 반려 상태의 초안을 승인 요청 상태로 변경 */
  public void submit(UUID actorId) {
    if (status != ProductContentStatus.GENERATED && status != ProductContentStatus.REJECTED) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "설명 초안을 승인 요청할 수 없습니다.");
    }
    this.status = ProductContentStatus.READY_FOR_APPROVAL;
    this.submittedByActorId = actorId;
    this.rejectReason = null;
  }

  /** 승인 요청 상태의 초안을 승인 완료 상태로 변경 */
  public void approve(UUID actorId) {
    if (status != ProductContentStatus.READY_FOR_APPROVAL) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "설명 초안을 승인할 수 없습니다.");
    }
    this.status = ProductContentStatus.APPROVED;
    this.approvedByActorId = actorId;
  }

  /** 승인 요청 상태의 초안을 반려 상태로 변경 */
  public void reject(String reason) {
    if (status != ProductContentStatus.READY_FOR_APPROVAL) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "설명 초안을 반려할 수 없습니다.");
    }
    if (reason == null || reason.isBlank()) {
      throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "반려 사유는 필수입니다.");
    }
    this.status = ProductContentStatus.REJECTED;
    this.rejectReason = reason;
  }
}
