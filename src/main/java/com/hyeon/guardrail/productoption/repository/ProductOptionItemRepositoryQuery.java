package com.hyeon.guardrail.productoption.repository;

import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.domain.QProductOptionItem;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 상품 옵션값 Querydsl 조회 저장소 */
@Repository
public class ProductOptionItemRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductOptionItemRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 옵션값 단건 조회 */
  public Optional<ProductOptionItem> findById(UUID productOptionId, UUID productOptionItemId) {
    QProductOptionItem productOptionItem = QProductOptionItem.productOptionItem;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(productOptionItem)
            .where(
                productOptionItem.id.eq(productOptionItemId),
                productOptionItem.productOptionId.eq(productOptionId),
                productOptionItem.deleted.isFalse())
            .fetchOne());
  }

  /** 삭제되지 않은 옵션값 목록 조회 */
  public List<ProductOptionItem> findAllByProductOptionId(
      UUID productOptionId, ProductOptionStatus status) {
    QProductOptionItem productOptionItem = QProductOptionItem.productOptionItem;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(productOptionItem.productOptionId.eq(productOptionId));
    condition.and(productOptionItem.deleted.isFalse());

    if (status != null) {
      condition.and(productOptionItem.status.eq(status));
    }

    return queryFactory
        .selectFrom(productOptionItem)
        .where(condition)
        .orderBy(productOptionItem.sortOrder.asc(), productOptionItem.createdAt.asc())
        .fetch();
  }

  /** 미삭제 옵션값 이름 존재 여부 조회 */
  public boolean existsByProductOptionIdAndName(
      UUID productOptionId, String name, UUID excludedId) {
    QProductOptionItem productOptionItem = QProductOptionItem.productOptionItem;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(productOptionItem.productOptionId.eq(productOptionId));
    condition.and(productOptionItem.name.eq(name));
    condition.and(productOptionItem.deleted.isFalse());

    if (excludedId != null) {
      condition.and(productOptionItem.id.ne(excludedId));
    }

    return queryFactory.selectOne().from(productOptionItem).where(condition).fetchFirst() != null;
  }

  /** 미삭제 옵션값 정렬 순서 존재 여부 조회 */
  public boolean existsByProductOptionIdAndSortOrder(
      UUID productOptionId, int sortOrder, UUID excludedId) {
    QProductOptionItem productOptionItem = QProductOptionItem.productOptionItem;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(productOptionItem.productOptionId.eq(productOptionId));
    condition.and(productOptionItem.sortOrder.eq(sortOrder));
    condition.and(productOptionItem.deleted.isFalse());

    if (excludedId != null) {
      condition.and(productOptionItem.id.ne(excludedId));
    }

    return queryFactory.selectOne().from(productOptionItem).where(condition).fetchFirst() != null;
  }

  /** 옵션 그룹 기준 다음 옵션값 정렬 순서 조회 */
  public int findNextSortOrderByProductOptionId(UUID productOptionId) {
    QProductOptionItem productOptionItem = QProductOptionItem.productOptionItem;
    NumberExpression<Integer> nextSortOrder = productOptionItem.sortOrder.max().coalesce(0).add(1);

    Integer result =
        queryFactory
            .select(nextSortOrder)
            .from(productOptionItem)
            .where(
                productOptionItem.productOptionId.eq(productOptionId),
                productOptionItem.deleted.isFalse())
            .fetchOne();

    return result == null ? 1 : result;
  }
}
