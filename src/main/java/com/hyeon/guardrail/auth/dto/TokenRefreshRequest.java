package com.hyeon.guardrail.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 토큰 재발급 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 재발급 요청")
public class TokenRefreshRequest {

  @NotNull
  @Schema(description = "세션 ID")
  private UUID sessionId;

  @NotBlank
  @Schema(description = "리프레시 토큰")
  private String refreshToken;
}
