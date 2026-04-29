package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.product.domain.ProductHistory;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.QProductHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 상품 처리 이력 Querydsl 조회 저장소 */
@Repository
public class ProductHistoryRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductHistoryRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 상품별 처리 이력 목록 조회 */
  public List<ProductHistory> findAllByProductId(UUID productId) {
    QProductHistory productHistory = QProductHistory.productHistory;

    return queryFactory
        .selectFrom(productHistory)
        .where(productHistory.productId.eq(productId))
        .orderBy(productHistory.createdAt.asc())
        .fetch();
  }

  /** 상품 생성 이력 기준 소유 여부 조회 */
  public boolean isProductOwner(UUID productId, UUID actorId) {
    QProductHistory productHistory = QProductHistory.productHistory;

    Integer fetched =
        queryFactory
            .selectOne()
            .from(productHistory)
            .where(
                productHistory.productId.eq(productId),
                productHistory.actorId.eq(actorId),
                productHistory.type.eq(ProductHistoryType.CREATED))
            .fetchFirst();

    return fetched != null;
  }
}
