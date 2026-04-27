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

  public void delete() {
    this.deleted = true;
  }

  public void restore() {
    this.deleted = false;
  }
}
