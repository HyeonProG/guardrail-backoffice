package com.hyeon.guardrail.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hyeon.guardrail.auth.domain.DeviceType;
import com.hyeon.guardrail.auth.domain.LoginResult;
import com.hyeon.guardrail.auth.domain.SessionStatus;
import com.hyeon.guardrail.auth.domain.UserLoginHistory;
import com.hyeon.guardrail.auth.domain.UserPasswordHistory;
import com.hyeon.guardrail.auth.domain.UserSession;
import com.hyeon.guardrail.auth.dto.ChangePasswordRequest;
import com.hyeon.guardrail.auth.dto.ChangePasswordResponse;
import com.hyeon.guardrail.auth.dto.LoginRequest;
import com.hyeon.guardrail.auth.dto.LoginResponse;
import com.hyeon.guardrail.auth.dto.LogoutResponse;
import com.hyeon.guardrail.auth.dto.TokenIssueResponse;
import com.hyeon.guardrail.auth.dto.TokenRefreshRequest;
import com.hyeon.guardrail.auth.dto.TokenRefreshResponse;
import com.hyeon.guardrail.auth.repository.AuthRepositoryQuery;
import com.hyeon.guardrail.auth.repository.UserLoginHistoryRepository;
import com.hyeon.guardrail.auth.repository.UserPasswordHistoryRepository;
import com.hyeon.guardrail.auth.repository.UserSessionRepository;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserRole;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/** 인증 서비스 단위 테스트 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserPasswordHistoryRepository passwordHistoryRepository;
  @Mock private UserLoginHistoryRepository loginHistoryRepository;
  @Mock private UserSessionRepository sessionRepository;
  @Mock private AuthRepositoryQuery authRepositoryQuery;
  @Mock private UserRepositoryQuery userRepositoryQuery;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private CurrentUserService currentUserService;

  @InjectMocks private AuthService authService;

  /** 초기 비밀번호 저장 시 해시와 만료일을 기록 */
  @Test
  void saveInitialPasswordStoresHashedPasswordWithExpiration() {
    UUID userId = UUID.randomUUID();
    when(passwordEncoder.encode("Temp1234!")).thenReturn("encoded-password");

    authService.saveInitialPassword(userId, "Temp1234!");

    ArgumentCaptor<UserPasswordHistory> captor = ArgumentCaptor.forClass(UserPasswordHistory.class);
    verify(passwordHistoryRepository).save(captor.capture());
    UserPasswordHistory saved = captor.getValue();
    assertThat(saved.getUserId()).isEqualTo(userId);
    assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
    assertThat(saved.isTemporary()).isTrue();
    assertThat(saved.getExpiredAt()).isAfter(LocalDateTime.now().plusDays(6));
  }

  /** 존재하지 않는 이메일 로그인 실패는 이력을 남기지 않는다 */
  @Test
  void loginWithUnknownEmailFailsWithoutSavingHistory() {
    LoginRequest request = new LoginRequest("missing@example.com", "password", DeviceType.WEB);
    when(userRepositoryQuery.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
        .isInstanceOf(BaseException.class);
    verify(loginHistoryRepository, never()).save(any(UserLoginHistory.class));
  }

  /** 비밀번호 불일치 로그인 실패는 실패 이력을 저장 */
  @Test
  void loginWithWrongPasswordSavesFailHistory() {
    UUID userId = UUID.randomUUID();
    User user = new User("staff@example.com", "홍길동", UserRole.STAFF, UserStatus.ACTIVE);
    ReflectionTestUtils.setField(user, "id", userId);
    UserPasswordHistory passwordHistory =
        new UserPasswordHistory(userId, "encoded-password", false, LocalDateTime.now().plusDays(1));
    LoginRequest request = new LoginRequest("staff@example.com", "wrong-password", DeviceType.WEB);

    when(userRepositoryQuery.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(authRepositoryQuery.findLatestValidPasswordHistory(eq(userId), any(LocalDateTime.class)))
        .thenReturn(Optional.of(passwordHistory));
    when(passwordEncoder.matches(request.getPassword(), passwordHistory.getPasswordHash()))
        .thenReturn(false);

    assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
        .isInstanceOf(BaseException.class);

    ArgumentCaptor<UserLoginHistory> captor = ArgumentCaptor.forClass(UserLoginHistory.class);
    verify(loginHistoryRepository).save(captor.capture());
    assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    assertThat(captor.getValue().getLoginResult()).isEqualTo(LoginResult.FAIL);
  }

  /** 로그인 성공 시 토큰 발급과 세션 저장을 수행 */
  @Test
  void loginSuccessIssuesTokensAndSavesSession() {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    User user = new User("staff@example.com", "홍길동", UserRole.STAFF, UserStatus.ACTIVE);
    ReflectionTestUtils.setField(user, "id", userId);
    UserPasswordHistory passwordHistory =
        new UserPasswordHistory(userId, "encoded-password", false, LocalDateTime.now().plusDays(1));
    LoginRequest request = new LoginRequest("staff@example.com", "plain-password", DeviceType.WEB);
    TokenIssueResponse accessToken =
        new TokenIssueResponse("access-token", LocalDateTime.now().plusMinutes(30));
    TokenIssueResponse refreshToken =
        new TokenIssueResponse("refresh-token", LocalDateTime.now().plusDays(14));

    when(userRepositoryQuery.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(authRepositoryQuery.findLatestValidPasswordHistory(eq(userId), any(LocalDateTime.class)))
        .thenReturn(Optional.of(passwordHistory));
    when(passwordEncoder.matches(request.getPassword(), passwordHistory.getPasswordHash()))
        .thenReturn(true);
    when(jwtService.issueAccessToken(
            eq(userId), eq(UserRole.STAFF), any(UUID.class), any(String.class)))
        .thenReturn(accessToken);
    when(jwtService.issueRefreshToken(eq(userId), any(UUID.class))).thenReturn(refreshToken);
    when(passwordEncoder.encode(refreshToken.getToken())).thenReturn("refresh-token-hash");
    when(sessionRepository.save(any(UserSession.class)))
        .thenAnswer(
            invocation -> {
              UserSession session = invocation.getArgument(0);
              ReflectionTestUtils.setField(session, "id", sessionId);
              return session;
            });

    LoginResponse response = authService.login(request, "127.0.0.1");

    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getSessionId()).isEqualTo(sessionId);
    verify(loginHistoryRepository).save(any(UserLoginHistory.class));
    verify(sessionRepository).save(any(UserSession.class));
  }

  /** 토큰 재발급 성공 시 세션 토큰 정보를 갱신 */
  @Test
  void refreshTokenUpdatesSession() {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    User user = new User("staff@example.com", "홍길동", UserRole.STAFF, UserStatus.ACTIVE);
    ReflectionTestUtils.setField(user, "id", userId);
    UserSession session =
        new UserSession(
            userId,
            "old-access-token-id",
            "old-refresh-token-hash",
            DeviceType.WEB,
            "127.0.0.1",
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1));
    ReflectionTestUtils.setField(session, "id", sessionId);
    TokenIssueResponse accessToken =
        new TokenIssueResponse("new-access-token", LocalDateTime.now().plusMinutes(30));
    TokenIssueResponse refreshToken =
        new TokenIssueResponse("new-refresh-token", LocalDateTime.now().plusDays(14));
    TokenRefreshRequest request = new TokenRefreshRequest(sessionId, "plain-refresh-token");
    Claims claims = mock(Claims.class);

    when(claims.get("sessionId", String.class)).thenReturn(sessionId.toString());
    when(jwtService.parseRefreshToken(request.getRefreshToken())).thenReturn(claims);
    when(authRepositoryQuery.findSessionById(sessionId)).thenReturn(Optional.of(session));
    when(passwordEncoder.matches(request.getRefreshToken(), session.getRefreshTokenHash()))
        .thenReturn(true);
    when(userRepositoryQuery.findById(userId)).thenReturn(Optional.of(user));
    when(jwtService.issueAccessToken(
            eq(userId), eq(UserRole.STAFF), eq(sessionId), any(String.class)))
        .thenReturn(accessToken);
    when(jwtService.issueRefreshToken(userId, sessionId)).thenReturn(refreshToken);
    when(passwordEncoder.encode(refreshToken.getToken())).thenReturn("new-refresh-token-hash");

    TokenRefreshResponse response = authService.refreshToken(request);

    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    assertThat(session.getRefreshTokenHash()).isEqualTo("new-refresh-token-hash");
    assertThat(session.getAccessTokenId()).isNotBlank();
  }

  /** 로그아웃 성공 시 세션 상태를 철회로 변경 */
  @Test
  void logoutRevokesSession() {
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    UserSession session =
        new UserSession(
            userId,
            "access-token-id",
            "refresh-token-hash",
            DeviceType.WEB,
            "127.0.0.1",
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(1));
    ReflectionTestUtils.setField(session, "id", sessionId);
    Claims claims = mock(Claims.class);

    when(claims.get("sessionId", String.class)).thenReturn(sessionId.toString());
    when(jwtService.parseAccessToken("access-token")).thenReturn(claims);
    when(authRepositoryQuery.findSessionById(sessionId)).thenReturn(Optional.of(session));

    LogoutResponse response = authService.logout("Bearer access-token", sessionId);

    assertThat(response.getSessionId()).isEqualTo(sessionId);
    assertThat(response.getStatus()).isEqualTo(SessionStatus.REVOKED);
    assertThat(session.getStatus()).isEqualTo(SessionStatus.REVOKED);
  }

  /** 현재 비밀번호가 일치하면 새 비밀번호 이력을 저장한다 */
  @Test
  void changePasswordStoresNewNonTemporaryPasswordHistory() {
    UUID userId = UUID.randomUUID();
    UserPasswordHistory latestPasswordHistory =
        new UserPasswordHistory(
            userId, "encoded-current-password", true, LocalDateTime.now().plusDays(1));
    ChangePasswordRequest request = new ChangePasswordRequest("current-password", "new-password");

    when(authRepositoryQuery.findLatestValidPasswordHistory(eq(userId), any(LocalDateTime.class)))
        .thenReturn(Optional.of(latestPasswordHistory));
    when(passwordEncoder.matches(
            request.getCurrentPassword(), latestPasswordHistory.getPasswordHash()))
        .thenReturn(true);
    when(passwordEncoder.matches(request.getNewPassword(), latestPasswordHistory.getPasswordHash()))
        .thenReturn(false);
    when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encoded-new-password");
    when(passwordHistoryRepository.save(any(UserPasswordHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ChangePasswordResponse response = authService.changePassword(userId, request);

    verify(currentUserService).validateActor(userId);
    ArgumentCaptor<UserPasswordHistory> captor = ArgumentCaptor.forClass(UserPasswordHistory.class);
    verify(passwordHistoryRepository).save(captor.capture());
    assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-new-password");
    assertThat(captor.getValue().isTemporary()).isFalse();
    assertThat(captor.getValue().getExpiredAt()).isNull();
    assertThat(response.isTemporary()).isFalse();
  }
}
