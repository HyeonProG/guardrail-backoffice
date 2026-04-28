package com.hyeon.guardrail.auth.service;

import com.hyeon.guardrail.auth.dto.TokenIssueResponse;
import com.hyeon.guardrail.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** JWT 발급과 검증 서비스 */
@Service
public class JwtService {

  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final String USER_ID_CLAIM = "userId";
  private static final String ROLE_CLAIM = "role";
  private static final String SESSION_ID_CLAIM = "sessionId";
  private static final String ACCESS_TOKEN_ID_CLAIM = "accessTokenId";

  private final SecretKey secretKey;
  private final long accessTokenExpirationSeconds;
  private final long refreshTokenExpirationSeconds;

  public JwtService(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds,
      @Value("${jwt.refresh-token-expiration-seconds}") long refreshTokenExpirationSeconds) {
    this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
  }

  /** 액세스 토큰 발급 */
  public TokenIssueResponse issueAccessToken(
      UUID userId, UserRole role, UUID sessionId, String accessTokenId) {
    LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(accessTokenExpirationSeconds);
    String token =
        Jwts.builder()
            .subject(userId.toString())
            .claim(USER_ID_CLAIM, userId.toString())
            .claim(ROLE_CLAIM, role.name())
            .claim(SESSION_ID_CLAIM, sessionId.toString())
            .claim(ACCESS_TOKEN_ID_CLAIM, accessTokenId)
            .issuedAt(toDate(LocalDateTime.now()))
            .expiration(toDate(expiredAt))
            .signWith(secretKey)
            .compact();

    return new TokenIssueResponse(token, expiredAt);
  }

  /** 리프레시 토큰 발급 */
  public TokenIssueResponse issueRefreshToken(UUID userId, UUID sessionId) {
    LocalDateTime expiredAt = LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds);
    String token =
        Jwts.builder()
            .subject(userId.toString())
            .claim(USER_ID_CLAIM, userId.toString())
            .claim(SESSION_ID_CLAIM, sessionId.toString())
            .issuedAt(toDate(LocalDateTime.now()))
            .expiration(toDate(expiredAt))
            .signWith(secretKey)
            .compact();

    return new TokenIssueResponse(token, expiredAt);
  }

  /** 액세스 토큰 claims 검증 */
  public Claims parseAccessToken(String accessToken) {
    return parseClaims(accessToken);
  }

  /** 리프레시 토큰 claims 검증 */
  public Claims parseRefreshToken(String refreshToken) {
    return parseClaims(refreshToken);
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  private Date toDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZONE_ID).toInstant());
  }
}
