package com.hyeon.guardrail.category.domain;

import com.hyeon.guardrail.common.domain.SoftDeleteEntity;
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

/** 상품 분류 체계 카테고리 엔티티 */
@Getter
@Entity
@Table(name = "categories")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends SoftDeleteEntity {

  @Column(name = "parent_id")
  private UUID parentId;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CategoryStatus status;

  /** 카테고리 기본 정보를 수정 */
  public void updateBasicInfo(UUID parentId, String name) {
    this.parentId = parentId;
    this.name = name;
  }

  /** 카테고리 상태를 운영 활성으로 변경 */
  public void activate() {
    this.status = CategoryStatus.ACTIVE;
  }

  /** 카테고리 상태를 운영 비활성으로 변경 */
  public void inactivate() {
    this.status = CategoryStatus.INACTIVE;
  }
}
