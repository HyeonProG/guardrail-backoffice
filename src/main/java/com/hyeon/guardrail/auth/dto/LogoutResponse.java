package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 로그아웃 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "로그아웃 응답")
public class LogoutResponse {

  @Schema(description = "세션 ID")
  private UUID sessionId;

  @Schema(description = "세션 상태", example = "REVOKED")
  private SessionStatus status;
}
