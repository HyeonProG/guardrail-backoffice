package com.hyeon.guardrail.auth.dto;

import com.hyeon.guardrail.auth.domain.LoginResult;
import com.hyeon.guardrail.auth.domain.LoginType;
import com.hyeon.guardrail.auth.domain.UserLoginHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 로그인 이력 응답 */
@Getter
@AllArgsConstructor
@Schema(description = "로그인 이력 응답")
public class LoginHistoryResponse {

  @Schema(description = "로그인 이력 ID")
  private UUID loginHistoryId;

  @Schema(description = "사용자 ID")
  private UUID userId;

  @Schema(description = "로그인 유형", example = "PASSWORD")
  private LoginType loginType;

  @Schema(description = "로그인 결과", example = "SUCCESS")
  private LoginResult loginResult;

  @Schema(description = "요청 IP", example = "127.0.0.1")
  private String ipAddress;

  @Schema(description = "로그인 일시", example = "2026-04-28T10:30:00")
  private LocalDateTime loggedInAt;

  /** 로그인 이력 엔티티를 응답으로 변환 */
  public static LoginHistoryResponse from(UserLoginHistory loginHistory) {
    return new LoginHistoryResponse(
        loginHistory.getId(),
        loginHistory.getUserId(),
        loginHistory.getLoginType(),
        loginHistory.getLoginResult(),
        loginHistory.getIpAddress(),
        loginHistory.getLoggedInAt());
  }
}
