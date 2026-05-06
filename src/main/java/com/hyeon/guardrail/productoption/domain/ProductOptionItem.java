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

/** 상품 옵션값 엔티티 */
@Getter
@Entity
@Table(name = "product_option_items")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductOptionItem extends SoftDeleteEntity {

  @Column(name = "product_option_id", nullable = false)
  private UUID productOptionId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "sort_order", nullable = false)
  private int sortOrder;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ProductOptionStatus status;

  /** 옵션값 기본 정보를 수정 */
  public void updateBasicInfo(String name) {
    this.name = name;
  }

  /** 옵션값 활성화 */
  public void activate() {
    this.status = ProductOptionStatus.ACTIVE;
  }

  /** 옵션값 비활성화 */
  public void inactivate() {
    this.status = ProductOptionStatus.INACTIVE;
  }
}
