package com.hyeon.guardrail.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** soft delete 대상 엔티티의 삭제 상태 정의 */
@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class SoftDeleteEntity extends BaseEntity {

  @Column(nullable = false)
  private boolean deleted = false;

  /** soft delete 상태로 전환 */
  public void delete() {
    this.deleted = true;
  }

  /** soft delete 상태 해제 */
  public void restore() {
    this.deleted = false;
  }
}
