package com.hyeon.guardrail.user.repository;

import com.hyeon.guardrail.user.domain.QUser;
import com.hyeon.guardrail.user.domain.User;
import com.hyeon.guardrail.user.domain.UserStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

  /** 삭제되지 않은 사용자 ID 목록 조회 */
  public List<User> findAllByIds(Collection<UUID> userIds) {
    QUser user = QUser.user;

    if (userIds == null || userIds.isEmpty()) {
      return List.of();
    }

    return queryFactory.selectFrom(user).where(user.id.in(userIds), user.deleted.isFalse()).fetch();
  }

  /** 삭제되지 않은 사용자 목록 조회 */
  public Page<User> findAll(UserStatus status, Pageable pageable) {
    QUser user = QUser.user;

    List<User> content =
        queryFactory
            .selectFrom(user)
            .where(
                status == null
                    ? user.deleted.isFalse()
                    : user.deleted.isFalse().and(user.status.eq(status)))
            .orderBy(user.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total =
        queryFactory
            .select(user.count())
            .from(user)
            .where(
                status == null
                    ? user.deleted.isFalse()
                    : user.deleted.isFalse().and(user.status.eq(status)))
            .fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0L : total);
  }
}
