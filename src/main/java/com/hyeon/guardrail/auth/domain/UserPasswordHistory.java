package com.hyeon.guardrail.auth.domain;

import com.hyeon.guardrail.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 비밀번호 변경 및 임시 비밀번호 이력 엔티티 */
@Getter
@Entity
@Table(name = "user_password_histories")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPasswordHistory extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "temporary", nullable = false)
  private boolean temporary;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;
}
