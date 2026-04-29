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

/** 사용자 인증 세션 엔티티 */
@Getter
@Entity
@Table(name = "user_sessions")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSession extends BaseEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "access_token_id", nullable = false)
  private String accessTokenId;

  @Column(name = "refresh_token_hash", nullable = false)
  private String refreshTokenHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "device_type", nullable = false)
  private DeviceType deviceType;

  @Column(name = "ip_address", nullable = false)
  private String ipAddress;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private SessionStatus status;

  @Column(name = "refreshed_at", nullable = false)
  private LocalDateTime refreshedAt;

  @Column(name = "expired_at", nullable = false)
  private LocalDateTime expiredAt;

  /** 세션 토큰 정보를 갱신 */
  public void refreshToken(
      String accessTokenId,
      String refreshTokenHash,
      LocalDateTime refreshedAt,
      LocalDateTime expiredAt) {
    this.accessTokenId = accessTokenId;
    this.refreshTokenHash = refreshTokenHash;
    this.refreshedAt = refreshedAt;
    this.expiredAt = expiredAt;
  }

  /** 세션을 활성 상태로 변경 */
  public void activate() {
    this.status = SessionStatus.ACTIVE;
  }

  /** 세션을 만료 상태로 변경 */
  public void expire() {
    this.status = SessionStatus.EXPIRED;
  }

  /** 세션을 철회 상태로 변경 */
  public void revoke() {
    this.status = SessionStatus.REVOKED;
  }
}
