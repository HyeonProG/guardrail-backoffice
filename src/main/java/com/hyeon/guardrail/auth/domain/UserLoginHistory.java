package com.hyeon.guardrail.auth.domain;

import com.hyeon.guardrail.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 로그인 이력 엔티티 */
@Getter
@Entity
@Table(name = "user_login_histories")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserLoginHistory extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "login_type", nullable = false)
  private LoginType loginType;

  @Enumerated(EnumType.STRING)
  @Column(name = "login_result", nullable = false)
  private LoginResult loginResult;

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Column(name = "logged_in_at", nullable = false)
  private LocalDateTime loggedInAt;
}
