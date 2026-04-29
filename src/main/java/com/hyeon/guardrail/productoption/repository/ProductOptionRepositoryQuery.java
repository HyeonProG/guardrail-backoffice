package com.hyeon.guardrail.productoption.repository;

import com.hyeon.guardrail.productoption.domain.ProductOption;
import com.hyeon.guardrail.productoption.domain.ProductOptionStatus;
import com.hyeon.guardrail.productoption.domain.QProductOption;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 상품 옵션 그룹 Querydsl 조회 저장소 */
@Repository
public class ProductOptionRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductOptionRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 옵션 그룹 단건 조회 */
  public Optional<ProductOption> findById(UUID categoryId, UUID productOptionId) {
    QProductOption productOption = QProductOption.productOption;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(productOption)
            .where(
                productOption.id.eq(productOptionId),
                productOption.categoryId.eq(categoryId),
                productOption.deleted.isFalse())
            .fetchOne());
  }

  /** 삭제되지 않은 옵션 그룹 목록 조회 */
  public List<ProductOption> findAllByCategoryId(UUID categoryId, ProductOptionStatus status) {
    QProductOption productOption = QProductOption.productOption;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(productOption.categoryId.eq(categoryId));
    condition.and(productOption.deleted.isFalse());

    if (status != null) {
      condition.and(productOption.status.eq(status));
    }

    return queryFactory
        .selectFrom(productOption)
        .where(condition)
        .orderBy(productOption.sortOrder.asc(), productOption.createdAt.asc())
        .fetch();
  }

  /** 미삭제 옵션 그룹 이름 존재 여부 조회 */
  public boolean existsByCategoryIdAndName(UUID categoryId, String name, UUID excludedId) {
    QProductOption productOption = QProductOption.productOption;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(productOption.categoryId.eq(categoryId));
    condition.and(productOption.name.eq(name));
    condition.and(productOption.deleted.isFalse());

    if (excludedId != null) {
      condition.and(productOption.id.ne(excludedId));
    }

    return queryFactory.selectOne().from(productOption).where(condition).fetchFirst() != null;
  }

  /** 미삭제 옵션 그룹 정렬 순서 존재 여부 조회 */
  public boolean existsByCategoryIdAndSortOrder(UUID categoryId, int sortOrder, UUID excludedId) {
    QProductOption productOption = QProductOption.productOption;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(productOption.categoryId.eq(categoryId));
    condition.and(productOption.sortOrder.eq(sortOrder));
    condition.and(productOption.deleted.isFalse());

    if (excludedId != null) {
      condition.and(productOption.id.ne(excludedId));
    }

    return queryFactory.selectOne().from(productOption).where(condition).fetchFirst() != null;
  }
}
