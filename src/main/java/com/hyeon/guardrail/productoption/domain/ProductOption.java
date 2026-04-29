package com.hyeon.guardrail.productoption.domain;

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

/** 상품 옵션 그룹 엔티티 */
@Getter
@Entity
@Table(name = "product_options")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOption extends SoftDeleteEntity {

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ProductOptionStatus status;

  /** 옵션 그룹 기본 정보를 수정 */
  public void updateBasicInfo(String name, int sortOrder) {
    this.name = name;
    this.sortOrder = sortOrder;
  }

  /** 옵션 그룹 활성화 */
  public void activate() {
    this.status = ProductOptionStatus.ACTIVE;
  }

  /** 옵션 그룹 비활성화 */
  public void inactivate() {
    this.status = ProductOptionStatus.INACTIVE;
  }
}
