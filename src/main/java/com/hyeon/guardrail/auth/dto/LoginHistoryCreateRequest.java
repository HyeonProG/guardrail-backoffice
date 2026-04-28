package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.LoginResult;
import com.hyeon.guardrail.auth.domain.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 로그인 이력 생성 요청 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 이력 생성 요청")
public class LoginHistoryCreateRequest {

  @NotNull
  @Schema(description = "사용자 ID")
  private UUID userId;

  @NotNull
  @Schema(description = "로그인 유형", example = "PASSWORD")
  private LoginType loginType;

  @NotNull
  @Schema(description = "로그인 결과", example = "SUCCESS")
  private LoginResult loginResult;

  @NotBlank
  @Schema(description = "요청 IP", example = "127.0.0.1")
  private String ipAddress;

  @NotNull
  @Schema(description = "로그인 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime loggedInAt;
}
