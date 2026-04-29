package com.hyeon.guardrail.auth.service;

import com.hyeon.guardrail.auth.domain.LoginResult;
import com.hyeon.guardrail.auth.domain.LoginType;
import com.hyeon.guardrail.auth.domain.SessionStatus;
import com.hyeon.guardrail.auth.domain.UserLoginHistory;
import com.hyeon.guardrail.auth.domain.UserPasswordHistory;
import com.hyeon.guardrail.auth.domain.UserSession;
import com.hyeon.guardrail.auth.dto.ChangePasswordRequest;
import com.hyeon.guardrail.auth.dto.ChangePasswordResponse;
import com.hyeon.guardrail.auth.dto.LoginHistoryCreateRequest;
import com.hyeon.guardrail.auth.dto.LoginHistoryResponse;
import com.hyeon.guardrail.auth.dto.LoginRequest;
import com.hyeon.guardrail.auth.dto.LoginResponse;
import com.hyeon.guardrail.auth.dto.LogoutResponse;
import com.hyeon.guardrail.auth.dto.PasswordHistoryResponse;
import com.hyeon.guardrail.auth.dto.SessionCreateRequest;
import com.hyeon.guardrail.auth.dto.SessionResponse;
import com.hyeon.guardrail.auth.dto.TokenIssueResponse;
import com.hyeon.guardrail.auth.dto.TokenRefreshRequest;
import com.hyeon.guardrail.auth.dto.TokenRefreshResponse;
import com.hyeon.guardrail.auth.repository.AuthRepositoryQuery;
import com.hyeon.guardrail.auth.repository.UserLoginHistoryRepository;
import com.hyeon.guardrail.auth.repository.UserPasswordHistoryRepository;
import com.hyeon.guardrail.auth.repository.UserSessionRepository;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.common.security.CurrentUserService;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.hyeon.guardrail.user.repository.UserRepositoryQuery;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 인증 이력과 세션 유스케이스 서비스 */
@Service
@RequiredArgsConstructor
public class AuthService {

  private static final int INITIAL_PASSWORD_EXPIRATION_DAYS = 7;
  private static final int BCRYPT_MAX_BYTES = 72;
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String SHA256_PREFIX = "{sha256}";
  private static final String SESSION_ID_CLAIM = "sessionId";

  private final UserPasswordHistoryRepository passwordHistoryRepository;
  private final UserLoginHistoryRepository loginHistoryRepository;
  private final UserSessionRepository sessionRepository;
  private final AuthRepositoryQuery authRepositoryQuery;
  private final UserRepositoryQuery userRepositoryQuery;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final CurrentUserService currentUserService;

  /** 사용자 초기 비밀번호 해시 저장 */
  @Transactional
  public void saveInitialPassword(UUID userId, String initialPassword) {
    String passwordHash = passwordEncoder.encode(initialPassword);
    LocalDateTime expiredAt = LocalDateTime.now().plusDays(INITIAL_PASSWORD_EXPIRATION_DAYS);
    UserPasswordHistory passwordHistory =
        new UserPasswordHistory(userId, passwordHash, true, expiredAt);

    passwordHistoryRepository.save(passwordHistory);
  }

  /** 로그인 검증과 토큰 발급 */
  @Transactional
  public LoginResponse login(LoginRequest request, String ipAddress) {
    User user =
        userRepositoryQuery
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다."));

    if (user.getStatus() != UserStatus.ACTIVE) {
      saveLoginHistory(user.getId(), LoginType.PASSWORD, LoginResult.FAIL, ipAddress);
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    UserPasswordHistory passwordHistory =
        authRepositoryQuery
            .findLatestValidPasswordHistory(user.getId(), LocalDateTime.now())
            .orElse(null);

    if (passwordHistory == null
        || !passwordEncoder.matches(request.getPassword(), passwordHistory.getPasswordHash())) {
      saveLoginHistory(user.getId(), LoginType.PASSWORD, LoginResult.FAIL, ipAddress);
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    LoginType loginType =
        passwordHistory.isTemporary() ? LoginType.TEMP_PASSWORD : LoginType.PASSWORD;
    saveLoginHistory(user.getId(), loginType, LoginResult.SUCCESS, ipAddress);

    UUID sessionId = UUID.randomUUID();
    String accessTokenId = UUID.randomUUID().toString();
    TokenIssueResponse accessToken =
        jwtService.issueAccessToken(user.getId(), user.getRole(), sessionId, accessTokenId);
    TokenIssueResponse refreshToken = jwtService.issueRefreshToken(user.getId(), sessionId);
    String refreshTokenHash = hashRefreshToken(refreshToken.getToken());

    UserSession session =
        new UserSession(
            user.getId(),
            accessTokenId,
            refreshTokenHash,
            request.getDeviceType(),
            ipAddress,
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            refreshToken.getExpiredAt());

    UserSession savedSession = sessionRepository.save(session);

    return new LoginResponse(
        accessToken.getToken(),
        refreshToken.getToken(),
        savedSession.getId(),
        accessToken.getExpiredAt(),
        refreshToken.getExpiredAt(),
        user.getId(),
        user.getRole());
  }

  /** 토큰 재발급 */
  @Transactional
  public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
    Claims claims = parseRefreshClaims(request.getRefreshToken());
    UUID tokenSessionId = UUID.fromString(claims.get(SESSION_ID_CLAIM, String.class));

    if (!request.getSessionId().equals(tokenSessionId)) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    UserSession session = findSession(request.getSessionId());
    LocalDateTime now = LocalDateTime.now();

    if (session.getStatus() != SessionStatus.ACTIVE || !session.getExpiredAt().isAfter(now)) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    if (!matchesRefreshToken(request.getRefreshToken(), session.getRefreshTokenHash())) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    User user =
        userRepositoryQuery
            .findById(session.getUserId())
            .orElseThrow(() -> new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다."));

    String accessTokenId = UUID.randomUUID().toString();
    TokenIssueResponse accessToken =
        jwtService.issueAccessToken(user.getId(), user.getRole(), session.getId(), accessTokenId);
    TokenIssueResponse refreshToken = jwtService.issueRefreshToken(user.getId(), session.getId());
    String refreshTokenHash = hashRefreshToken(refreshToken.getToken());

    sessionRepository.refreshToken(
        session.getId(),
        accessTokenId,
        refreshTokenHash,
        LocalDateTime.now(),
        refreshToken.getExpiredAt());

    return new TokenRefreshResponse(
        accessToken.getToken(),
        refreshToken.getToken(),
        session.getId(),
        accessToken.getExpiredAt(),
        refreshToken.getExpiredAt());
  }

  /** 로그아웃 */
  @Transactional
  public LogoutResponse logout(String authorizationHeader, UUID sessionId) {
    Claims claims = parseAccessClaims(extractAccessToken(authorizationHeader));
    UUID tokenSessionId = UUID.fromString(claims.get(SESSION_ID_CLAIM, String.class));

    if (!sessionId.equals(tokenSessionId)) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }

    UserSession session = findSession(sessionId);
    session.revoke();

    return new LogoutResponse(session.getId(), session.getStatus());
  }

  /** 사용자 비밀번호 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<PasswordHistoryResponse> getPasswordHistories(UUID userId) {
    return authRepositoryQuery.findPasswordHistoriesByUserId(userId).stream()
        .map(PasswordHistoryResponse::from)
        .toList();
  }

  /** 현재 로그인 사용자의 비밀번호를 변경한다. */
  @Transactional
  public ChangePasswordResponse changePassword(UUID userId, ChangePasswordRequest request) {
    currentUserService.validateActor(userId);

    UserPasswordHistory latestPasswordHistory =
        authRepositoryQuery
            .findLatestValidPasswordHistory(userId, LocalDateTime.now())
            .orElseThrow(
                () -> new BaseException(BaseResponseStatus.NOT_FOUND, "현재 비밀번호 정보를 찾을 수 없습니다."));

    if (!passwordEncoder.matches(
        request.getCurrentPassword(), latestPasswordHistory.getPasswordHash())) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
    }

    if (passwordEncoder.matches(
        request.getNewPassword(), latestPasswordHistory.getPasswordHash())) {
      throw new BaseException(BaseResponseStatus.CONFLICT, "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    UserPasswordHistory changedPasswordHistory =
        new UserPasswordHistory(
            userId, passwordEncoder.encode(request.getNewPassword()), false, null);

    UserPasswordHistory savedPasswordHistory =
        passwordHistoryRepository.save(changedPasswordHistory);
    return new ChangePasswordResponse(
        savedPasswordHistory.getUserId(), false, savedPasswordHistory.getCreatedAt());
  }

  /** 로그인 이력 저장 */
  @Transactional
  public LoginHistoryResponse createLoginHistory(LoginHistoryCreateRequest request) {
    UserLoginHistory loginHistory =
        new UserLoginHistory(
            request.getUserId(),
            request.getLoginType(),
            request.getLoginResult(),
            request.getIpAddress(),
            request.getLoggedInAt());

    return LoginHistoryResponse.from(loginHistoryRepository.save(loginHistory));
  }

  /** 사용자 로그인 이력 목록 조회 */
  @Transactional(readOnly = true)
  public List<LoginHistoryResponse> getLoginHistories(UUID userId) {
    return authRepositoryQuery.findLoginHistoriesByUserId(userId).stream()
        .map(LoginHistoryResponse::from)
        .toList();
  }

  /** 인증 세션 저장 */
  @Transactional
  public SessionResponse createSession(SessionCreateRequest request) {
    String refreshTokenHash = hashRefreshToken(request.getRefreshToken());
    UserSession session =
        new UserSession(
            request.getUserId(),
            request.getAccessTokenId(),
            refreshTokenHash,
            request.getDeviceType(),
            request.getIpAddress(),
            SessionStatus.ACTIVE,
            LocalDateTime.now(),
            request.getExpiredAt());

    return SessionResponse.from(sessionRepository.save(session));
  }

  /** 사용자 인증 세션 목록 조회 */
  @Transactional(readOnly = true)
  public List<SessionResponse> getSessions(UUID userId) {
    return authRepositoryQuery.findSessionsByUserId(userId).stream()
        .map(SessionResponse::from)
        .toList();
  }

  /** 인증 세션 상태 변경 */
  @Transactional
  public SessionResponse updateSessionStatus(UUID sessionId, SessionStatus status) {
    UserSession session = findSession(sessionId);

    if (status == SessionStatus.ACTIVE) {
      session.activate();
    } else if (status == SessionStatus.EXPIRED) {
      session.expire();
    } else {
      session.revoke();
    }

    return SessionResponse.from(session);
  }

  private void saveLoginHistory(
      UUID userId, LoginType loginType, LoginResult loginResult, String ipAddress) {
    UserLoginHistory loginHistory =
        new UserLoginHistory(userId, loginType, loginResult, ipAddress, LocalDateTime.now());
    loginHistoryRepository.save(loginHistory);
  }

  private UserSession findSession(UUID sessionId) {
    return authRepositoryQuery
        .findSessionById(sessionId)
        .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FOUND, "세션을 찾을 수 없습니다."));
  }

  private Claims parseAccessClaims(String accessToken) {
    try {
      return jwtService.parseAccessToken(accessToken);
    } catch (JwtException | IllegalArgumentException exception) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }
  }

  private Claims parseRefreshClaims(String refreshToken) {
    try {
      return jwtService.parseRefreshToken(refreshToken);
    } catch (JwtException | IllegalArgumentException exception) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }
  }

  private String extractAccessToken(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }

    return authorizationHeader.substring(BEARER_PREFIX.length());
  }

  private String hashRefreshToken(String refreshToken) {
    if (refreshToken.getBytes(StandardCharsets.UTF_8).length <= BCRYPT_MAX_BYTES) {
      return passwordEncoder.encode(refreshToken);
    }

    return SHA256_PREFIX + sha256(refreshToken);
  }

  private boolean matchesRefreshToken(String refreshToken, String refreshTokenHash) {
    if (refreshTokenHash != null && refreshTokenHash.startsWith(SHA256_PREFIX)) {
      return refreshTokenHash.equals(SHA256_PREFIX + sha256(refreshToken));
    }

    return passwordEncoder.matches(refreshToken, refreshTokenHash);
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hashed);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
    }
  }
}
