package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.DeviceType;
import com.hyeon.guardrail.auth.domain.SessionStatus;
import com.hyeon.guardrail.auth.domain.UserSession;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 인증 세션 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "인증 세션 응답")
public class SessionResponse {

  @Schema(description = "세션 ID")
  private UUID sessionId;

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "액세스 토큰 식별 정보")
  private String accessTokenId;

  @Schema(description = "디바이스 유형", example = "WEB")
  private DeviceType deviceType;

  @Schema(description = "요청 IP", example = "127.0.0.1")
  private String ipAddress;

  @Schema(description = "세션 상태", example = "ACTIVE")
  private SessionStatus status;

  @Schema(description = "갱신 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime refreshedAt;

  @Schema(description = "만료 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime expiredAt;

  /** 인증 세션 엔티티를 응답으로 변환 */
  public static SessionResponse from(UserSession session) {
    return new SessionResponse(
        session.getId(),
        session.getUserId(),
        session.getAccessTokenId(),
        session.getDeviceType(),
        session.getIpAddress(),
        session.getStatus(),
        session.getRefreshedAt(),
        session.getExpiredAt());
  }
}
