package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.DeviceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 인증 세션 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인증 세션 생성 요청")
public class SessionCreateRequest {

  @NotNull
  @Schema(description = "사용자 ID")
  private UUID userId;

  @NotBlank
  @Schema(description = "액세스 토큰 식별 정보")
  private String accessTokenId;

  @NotBlank
  @Schema(description = "리프레시 토큰 원문")
  private String refreshToken;

  @NotNull
  @Schema(description = "디바이스 유형", example = "WEB")
  private DeviceType deviceType;

  @NotBlank
  @Schema(description = "요청 IP", example = "127.0.0.1")
  private String ipAddress;

  @NotNull
  @Schema(description = "만료 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime expiredAt;
}
