package com.hyeon.guardrail.productcontent.domain;

import com.hyeon.guardrail.common.domain.BaseEntity;
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

/** 상품 설명 생성과 검수 처리 이력 엔티티 */
@Getter
@Entity
@Table(name = "product_content_histories")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductContentHistory extends BaseEntity {

  @Column(name = "draft_id", nullable = false)
  private UUID draftId;

  @Column(name = "product_id", nullable = false)
  private UUID productId;

  @Column(name = "actor_id", nullable = false)
  private UUID actorId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ProductContentHistoryType type;

  @Column(name = "reason", length = 1000)
  private String reason;
}
