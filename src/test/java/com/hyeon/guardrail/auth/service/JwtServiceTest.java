package com.hyeon.guardrail.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyeon.guardrail.auth.dto.TokenIssueResponse;
import com.hyeon.guardrail.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** JWT 서비스 단위 테스트 */
class JwtServiceTest {

  private static final String SECRET_KEY =
      "fa8ce82b9957cd8e40ff67462022c4694e3e7c1e7d545c8d010e5a141808b39d";

  /** 액세스 토큰 발급 후 claim을 복원 */
  @Test
  void issueAndParseAccessToken() {
    JwtService jwtService = new JwtService(SECRET_KEY, 1800L, 1209600L);
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    String accessTokenId = UUID.randomUUID().toString();

    TokenIssueResponse token =
        jwtService.issueAccessToken(userId, UserRole.ADMIN, sessionId, accessTokenId);
    Claims claims = jwtService.parseAccessToken(token.getToken());

    assertThat(claims.getSubject()).isEqualTo(userId.toString());
    assertThat(claims.get("userId", String.class)).isEqualTo(userId.toString());
    assertThat(claims.get("role", String.class)).isEqualTo(UserRole.ADMIN.name());
    assertThat(claims.get("sessionId", String.class)).isEqualTo(sessionId.toString());
    assertThat(claims.get("accessTokenId", String.class)).isEqualTo(accessTokenId);
  }

  /** 리프레시 토큰 발급 후 claim을 복원 */
  @Test
  void issueAndParseRefreshToken() {
    JwtService jwtService = new JwtService(SECRET_KEY, 1800L, 1209600L);
    UUID userId = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();

    TokenIssueResponse token = jwtService.issueRefreshToken(userId, sessionId);
    Claims claims = jwtService.parseRefreshToken(token.getToken());

    assertThat(claims.getSubject()).isEqualTo(userId.toString());
    assertThat(claims.get("userId", String.class)).isEqualTo(userId.toString());
    assertThat(claims.get("sessionId", String.class)).isEqualTo(sessionId.toString());
  }
}
