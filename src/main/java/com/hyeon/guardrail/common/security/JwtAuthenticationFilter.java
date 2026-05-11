package com.hyeon.guardrail.common.security;

import com.hyeon.guardrail.auth.domain.SessionStatus;
import com.hyeon.guardrail.auth.domain.UserSession;
import com.hyeon.guardrail.auth.repository.AuthRepositoryQuery;
import com.hyeon.guardrail.auth.service.JwtService;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Bearer JWT를 검증하고 SecurityContext에 로그인 사용자 정보를 적재한다. */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final AuthRepositoryQuery authRepositoryQuery;

  /** 공개 엔드포인트를 제외한 요청의 Bearer JWT를 검증한다. */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증 토큰이 필요합니다.");
    }

    String token = authorizationHeader.substring(7);

    try {
      Claims claims = jwtService.parseAccessToken(token);
      UUID userId = UUID.fromString(String.valueOf(claims.get("userId")));
      UserRole role = UserRole.valueOf(String.valueOf(claims.get("role")));
      UUID sessionId = UUID.fromString(String.valueOf(claims.get("sessionId")));
      String accessTokenId = String.valueOf(claims.get("accessTokenId"));

      validateSession(userId, sessionId, accessTokenId);

      AuthenticatedUser authenticatedUser = new AuthenticatedUser(userId, role);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              authenticatedUser, token, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (JwtException | IllegalArgumentException exception) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다.");
    } finally {
      SecurityContextHolder.clearContext();
    }
  }

  private void validateSession(UUID userId, UUID sessionId, String accessTokenId) {
    UserSession session =
        authRepositoryQuery
            .findSessionById(sessionId)
            .orElseThrow(() -> new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다."));

    if (!session.getUserId().equals(userId)
        || session.getStatus() != SessionStatus.ACTIVE
        || !session.getAccessTokenId().equals(accessTokenId)
        || !session.getExpiredAt().isAfter(LocalDateTime.now())) {
      throw new BaseException(BaseResponseStatus.UNAUTHORIZED, "인증에 실패했습니다.");
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return HttpMethod.OPTIONS.matches(request.getMethod())
        || path.startsWith("/swagger-ui")
        || path.startsWith("/api-docs")
        || path.startsWith("/uploads/")
        || "/swagger-ui.html".equals(path)
        || "/health".equals(path)
        || "/api/v1/health".equals(path)
        || "/api/v1/auth/login".equals(path)
        || "/api/v1/auth/logout".equals(path)
        || "/api/v1/auth/token/refresh".equals(path);
  }
}
