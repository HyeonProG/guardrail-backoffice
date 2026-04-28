package com.hyeon.guardrail.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 토큰 재발급 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "토큰 재발급 응답")
public class TokenRefreshResponse {

  @Schema(description = "액세스 토큰")
  private String accessToken;

  @Schema(description = "리프레시 토큰")
  private String refreshToken;

  @Schema(description = "세션 ID")
  private UUID sessionId;

  @Schema(description = "액세스 토큰 만료 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime accessTokenExpiredAt;

  @Schema(description = "리프레시 토큰 만료 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime refreshTokenExpiredAt;
}
