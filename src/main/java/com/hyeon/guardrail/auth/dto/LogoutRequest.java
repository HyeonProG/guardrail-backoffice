package com.hyeon.guardrail.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그아웃 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그아웃 요청")
public class LogoutRequest {

  @NotNull
  @Schema(description = "세션 ID")
  private UUID sessionId;
}
