package com.hyeon.guardrail.auth.controller;

import com.hyeon.guardrail.auth.dto.ChangePasswordRequest;
import com.hyeon.guardrail.auth.dto.ChangePasswordResponse;
import com.hyeon.guardrail.auth.dto.LoginHistoryCreateRequest;
import com.hyeon.guardrail.auth.dto.LoginHistoryResponse;
import com.hyeon.guardrail.auth.dto.LoginRequest;
import com.hyeon.guardrail.auth.dto.LoginResponse;
import com.hyeon.guardrail.auth.dto.LogoutRequest;
import com.hyeon.guardrail.auth.dto.LogoutResponse;
import com.hyeon.guardrail.auth.dto.PasswordHistoryResponse;
import com.hyeon.guardrail.auth.dto.SessionCreateRequest;
import com.hyeon.guardrail.auth.dto.SessionResponse;
import com.hyeon.guardrail.auth.dto.SessionStatusUpdateRequest;
import com.hyeon.guardrail.auth.dto.TokenRefreshRequest;
import com.hyeon.guardrail.auth.dto.TokenRefreshResponse;
import com.hyeon.guardrail.auth.service.AuthService;
import com.hyeon.guardrail.common.response.BaseResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 관리 API 컨트롤러 */
@Tag(name = "Auth", description = "인증 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  /** 로그인 */
  @Operation(summary = "로그인", description = "이메일과 비밀번호를 검증하고 토큰과 세션 정보를 반환합니다.")
  @PostMapping("/login")
  public BaseResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
    return BaseResponseEntity.success(
        authService.login(request, extractIpAddress(servletRequest)), "로그인되었습니다.");
  }

  /** 토큰 재발급 */
  @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 검증하고 새 토큰을 발급합니다.")
  @PostMapping("/token/refresh")
  public BaseResponseEntity<TokenRefreshResponse> refreshToken(
      @Valid @RequestBody TokenRefreshRequest request) {
    return BaseResponseEntity.success(authService.refreshToken(request), "토큰이 재발급되었습니다.");
  }

  /** 로그아웃 */
  @Operation(summary = "로그아웃", description = "세션을 종료하고 로그아웃 처리합니다.")
  @PostMapping("/logout")
  public BaseResponseEntity<LogoutResponse> logout(@Valid @RequestBody LogoutRequest request) {
    return BaseResponseEntity.success(authService.logout(request.getSessionId()), "로그아웃되었습니다.");
  }

  /** 사용자 비밀번호 이력 조회 */
  @Operation(summary = "비밀번호 이력 조회", description = "사용자의 비밀번호 이력 목록을 조회합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/users/{userId}/password-histories")
  public BaseResponseEntity<List<PasswordHistoryResponse>> getPasswordHistories(
      @PathVariable UUID userId) {
    return BaseResponseEntity.success(authService.getPasswordHistories(userId));
  }

  /** 사용자 비밀번호 변경 */
  @Operation(summary = "비밀번호 변경", description = "현재 로그인한 사용자의 비밀번호를 변경합니다.")
  @PatchMapping("/users/{userId}/password")
  public BaseResponseEntity<ChangePasswordResponse> changePassword(
      @PathVariable UUID userId, @Valid @RequestBody ChangePasswordRequest request) {
    return BaseResponseEntity.success(
        authService.changePassword(userId, request), "비밀번호가 변경되었습니다.");
  }

  /** 로그인 이력 저장 */
  @Operation(summary = "로그인 이력 저장", description = "사용자 로그인 이력을 저장합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/login-histories")
  public BaseResponseEntity<LoginHistoryResponse> createLoginHistory(
      @Valid @RequestBody LoginHistoryCreateRequest request) {
    return BaseResponseEntity.created(authService.createLoginHistory(request), "로그인 이력이 저장되었습니다.");
  }

  /** 사용자 로그인 이력 조회 */
  @Operation(summary = "로그인 이력 조회", description = "사용자의 로그인 이력 목록을 조회합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/users/{userId}/login-histories")
  public BaseResponseEntity<List<LoginHistoryResponse>> getLoginHistories(
      @PathVariable UUID userId) {
    return BaseResponseEntity.success(authService.getLoginHistories(userId));
  }

  /** 인증 세션 저장 */
  @Operation(summary = "세션 저장", description = "사용자 인증 세션을 저장합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @PostMapping("/sessions")
  public BaseResponseEntity<SessionResponse> createSession(
      @Valid @RequestBody SessionCreateRequest request) {
    return BaseResponseEntity.created(authService.createSession(request), "세션이 저장되었습니다.");
  }

  /** 사용자 인증 세션 조회 */
  @Operation(summary = "세션 조회", description = "사용자의 인증 세션 목록을 조회합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @GetMapping("/users/{userId}/sessions")
  public BaseResponseEntity<List<SessionResponse>> getSessions(@PathVariable UUID userId) {
    return BaseResponseEntity.success(authService.getSessions(userId));
  }

  /** 인증 세션 상태 변경 */
  @Operation(summary = "세션 상태 변경", description = "인증 세션 상태를 변경합니다.")
  @SecurityRequirement(name = "bearerAuth")
  @PatchMapping("/sessions/{sessionId}/status")
  public BaseResponseEntity<SessionResponse> updateSessionStatus(
      @PathVariable UUID sessionId, @Valid @RequestBody SessionStatusUpdateRequest request) {
    return BaseResponseEntity.success(
        authService.updateSessionStatus(sessionId, request.getStatus()), "세션 상태가 변경되었습니다.");
  }

  private String extractIpAddress(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isBlank()) {
      return forwardedFor.split(",")[0].trim();
    }

    return request.getRemoteAddr();
  }
}
