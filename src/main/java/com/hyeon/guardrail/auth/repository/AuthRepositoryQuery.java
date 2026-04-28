package com.hyeon.guardrail.auth.repository;

import com.hyeon.guardrail.auth.domain.QUserLoginHistory;
import com.hyeon.guardrail.auth.domain.QUserPasswordHistory;
import com.hyeon.guardrail.auth.domain.QUserSession;
import com.hyeon.guardrail.auth.domain.UserLoginHistory;
import com.hyeon.guardrail.auth.domain.UserPasswordHistory;
import com.hyeon.guardrail.auth.domain.UserSession;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 인증 도메인 Querydsl 조회 저장소 */
@Repository
public class AuthRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public AuthRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 사용자 비밀번호 이력 목록 조회 */
  public List<UserPasswordHistory> findPasswordHistoriesByUserId(UUID userId) {
    QUserPasswordHistory passwordHistory = QUserPasswordHistory.userPasswordHistory;

    return queryFactory
        .selectFrom(passwordHistory)
        .where(passwordHistory.userId.eq(userId))
        .orderBy(passwordHistory.createdAt.desc())
        .fetch();
  }

  /** 사용자 최근 유효 비밀번호 이력 조회 */
  public Optional<UserPasswordHistory> findLatestValidPasswordHistory(
      UUID userId, LocalDateTime now) {
    QUserPasswordHistory passwordHistory = QUserPasswordHistory.userPasswordHistory;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(passwordHistory)
            .where(
                passwordHistory.userId.eq(userId),
                passwordHistory.expiredAt.isNull().or(passwordHistory.expiredAt.after(now)))
            .orderBy(passwordHistory.createdAt.desc())
            .fetchFirst());
  }

  /** 사용자 로그인 이력 목록 조회 */
  public List<UserLoginHistory> findLoginHistoriesByUserId(UUID userId) {
    QUserLoginHistory loginHistory = QUserLoginHistory.userLoginHistory;

    return queryFactory
        .selectFrom(loginHistory)
        .where(loginHistory.userId.eq(userId))
        .orderBy(loginHistory.loggedInAt.desc())
        .fetch();
  }

  /** 사용자 세션 목록 조회 */
  public List<UserSession> findSessionsByUserId(UUID userId) {
    QUserSession userSession = QUserSession.userSession;

    return queryFactory
        .selectFrom(userSession)
        .where(userSession.userId.eq(userId))
        .orderBy(userSession.createdAt.desc())
        .fetch();
  }

  /** 사용자 세션 단건 조회 */
  public Optional<UserSession> findSessionById(UUID sessionId) {
    QUserSession userSession = QUserSession.userSession;

    return Optional.ofNullable(
        queryFactory.selectFrom(userSession).where(userSession.id.eq(sessionId)).fetchOne());
  }
}
