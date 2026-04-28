package com.hyeon.guardrail.auth.repository;

import com.hyeon.guardrail.auth.domain.UserSession;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 사용자 인증 세션 저장소 */
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

  /** 세션 토큰 정보 갱신 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update UserSession session
         set session.accessTokenId = :accessTokenId,
             session.refreshTokenHash = :refreshTokenHash,
             session.refreshedAt = :refreshedAt,
             session.expiredAt = :expiredAt
       where session.id = :sessionId
      """)
  int refreshToken(
      @Param("sessionId") UUID sessionId,
      @Param("accessTokenId") String accessTokenId,
      @Param("refreshTokenHash") String refreshTokenHash,
      @Param("refreshedAt") LocalDateTime refreshedAt,
      @Param("expiredAt") LocalDateTime expiredAt);
}
