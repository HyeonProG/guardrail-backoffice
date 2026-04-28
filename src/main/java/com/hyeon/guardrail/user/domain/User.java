package com.hyeon.guardrail.user.domain;

import com.hyeon.guardrail.common.domain.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시스템 사용자 기본 정보 엔티티 */
@Getter
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends SoftDeleteEntity {

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private UserStatus status;

  /** 사용자 상태를 사용으로 변경 */
  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  /** 사용자 상태를 미사용으로 변경 */
  public void inactivate() {
    this.status = UserStatus.INACTIVE;
  }
}
