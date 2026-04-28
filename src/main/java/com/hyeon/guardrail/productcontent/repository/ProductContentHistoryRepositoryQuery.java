package com.hyeon.guardrail.productcontent.repository;

import com.hyeon.guardrail.productcontent.domain.ProductContentHistory;
import com.hyeon.guardrail.productcontent.domain.QProductContentHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 상품 설명 처리 이력 Querydsl 조회 저장소 */
@Repository
public class ProductContentHistoryRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductContentHistoryRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 초안별 처리 이력 목록 조회 */
  public List<ProductContentHistory> findAllByDraftId(UUID productId, UUID draftId) {
    QProductContentHistory history = QProductContentHistory.productContentHistory;

    return queryFactory
        .selectFrom(history)
        .where(history.productId.eq(productId), history.draftId.eq(draftId))
        .orderBy(history.createdAt.asc())
        .fetch();
  }
}
