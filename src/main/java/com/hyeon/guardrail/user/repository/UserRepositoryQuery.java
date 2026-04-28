package com.hyeon.guardrail.user.repository;

import com.hyeon.guardrail.user.domain.QUser;
import com.hyeon.guardrail.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 사용자 Querydsl 조회 저장소 */
@Repository
public class UserRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public UserRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 사용자 단건 조회 */
  public Optional<User> findById(UUID userId) {
    QUser user = QUser.user;

    return Optional.ofNullable(
        queryFactory.selectFrom(user).where(user.id.eq(userId), user.deleted.isFalse()).fetchOne());
  }

  /** 삭제되지 않은 사용자 이메일 조회 */
  public Optional<User> findByEmail(String email) {
    QUser user = QUser.user;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(user)
            .where(user.email.eq(email), user.deleted.isFalse())
            .fetchOne());
  }
}
