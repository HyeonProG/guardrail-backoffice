package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.SessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 인증 세션 상태 변경 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인증 세션 상태 변경 요청")
public class SessionStatusUpdateRequest {

  @NotNull
  @Schema(description = "세션 상태", example = "REVOKED")
  private SessionStatus status;
}
