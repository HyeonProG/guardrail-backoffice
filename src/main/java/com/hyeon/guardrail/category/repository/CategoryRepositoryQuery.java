package com.hyeon.guardrail.category.repository;

import com.hyeon.guardrail.category.domain.Category;
import com.hyeon.guardrail.category.domain.CategoryStatus;
import com.hyeon.guardrail.category.domain.QCategory;
import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/** 카테고리 Querydsl 조회 저장소 */
@Repository
public class CategoryRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public CategoryRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 카테고리 단건 조회 */
  public Optional<Category> findById(UUID categoryId) {
    QCategory category = QCategory.category;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(category)
            .where(category.id.eq(categoryId), category.deleted.isFalse())
            .fetchOne());
  }

  /** 삭제된 카테고리 단건 조회 */
  public Optional<Category> findDeletedById(UUID categoryId) {
    QCategory category = QCategory.category;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(category)
            .where(category.id.eq(categoryId), category.deleted.isTrue())
            .fetchOne());
  }

  /** 삭제되지 않은 카테고리 목록 조회 */
  public Page<Category> findAll(
      UUID parentId, boolean filterByParent, CategoryStatus status, Pageable pageable) {
    QCategory category = QCategory.category;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(category.deleted.isFalse());

    if (filterByParent) {
      if (parentId == null) {
        condition.and(category.parentId.isNull());
      } else {
        condition.and(category.parentId.eq(parentId));
      }
    }

    if (status != null) {
      condition.and(category.status.eq(status));
    }

    List<Category> content =
        queryFactory
            .selectFrom(category)
            .where(condition)
            .orderBy(toOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = queryFactory.select(category.count()).from(category).where(condition).fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0L : total);
  }

  /** 삭제된 카테고리 목록 조회 */
  public List<Category> findDeletedAll() {
    QCategory category = QCategory.category;

    return queryFactory
        .selectFrom(category)
        .where(category.deleted.isTrue())
        .orderBy(category.updatedAt.desc(), category.createdAt.desc())
        .fetch();
  }

  private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
    QCategory category = QCategory.category;

    if (sort.isUnsorted()) {
      return new OrderSpecifier<?>[] {category.createdAt.desc()};
    }

    return sort.stream()
        .map(order -> toOrderSpecifier(category, order))
        .toArray(OrderSpecifier[]::new);
  }

  private OrderSpecifier<?> toOrderSpecifier(QCategory category, Sort.Order sortOrder) {
    Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

    return switch (sortOrder.getProperty()) {
      case "createdAt" -> new OrderSpecifier<>(direction, category.createdAt);
      case "updatedAt" -> new OrderSpecifier<>(direction, category.updatedAt);
      case "name" -> new OrderSpecifier<>(direction, category.name);
      case "status" -> new OrderSpecifier<>(direction, category.status);
      default -> throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "허용되지 않은 정렬 필드입니다.");
    };
  }
}
