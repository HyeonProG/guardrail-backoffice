package com.hyeon.guardrail.product.domain;

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

/** 상품 기본 정보와 등록 상태 엔티티 */
@Getter
@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends SoftDeleteEntity {

  @Column(name = "category_id", nullable = false)
  private UUID categoryId;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description", nullable = false, length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ProductStatus status;

  /** 상품 기본 정보를 수정 */
  public void updateBasicInfo(UUID categoryId, String name, String description) {
    this.categoryId = categoryId;
    this.name = name;
    this.description = description;
  }

  /** 상품 설명을 반영한다 */
  public void updateDescription(String description) {
    this.description = description;
  }

  /** 상품을 승인 대기 상태로 제출 */
  public void submit() {
    if (status != ProductStatus.DRAFT && status != ProductStatus.REJECTED) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "상품을 승인 대기 상태로 변경할 수 없습니다.");
    }
    this.status = ProductStatus.PENDING;
  }

  /** 승인 대기 상품을 승인 완료 상태로 변경 */
  public void approve() {
    if (status != ProductStatus.PENDING) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "상품을 승인할 수 없습니다.");
    }
    this.status = ProductStatus.APPROVED;
  }

  /** 승인 대기 상품을 반려 상태로 변경 */
  public void reject() {
    if (status != ProductStatus.PENDING) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "상품을 반려할 수 없습니다.");
    }
    this.status = ProductStatus.REJECTED;
  }

  /** 승인 완료 상품을 비활성 상태로 변경 */
  public void inactivate() {
    if (status != ProductStatus.APPROVED) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "상품을 비활성화할 수 없습니다.");
    }
    this.status = ProductStatus.INACTIVE;
  }
}
