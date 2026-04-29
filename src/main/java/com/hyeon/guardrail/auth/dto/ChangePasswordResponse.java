package com.hyeon.guardrail.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 비밀번호 변경 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "비밀번호 변경 응답")
public class ChangePasswordResponse {

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "임시 비밀번호 여부", example = "false")
  private boolean temporary;

  @Schema(description = "변경 일시", example = "2026-04-30T10:30:00")
  private LocalDateTime changedAt;
}
