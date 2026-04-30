package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.product.domain.ProductSelectedOption;
import com.hyeon.guardrail.product.domain.QProductSelectedOption;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 상품 선택 옵션 Querydsl 조회 저장소 */
@Repository
public class ProductSelectedOptionRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductSelectedOptionRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 상품 선택 옵션 목록 조회 */
  public List<ProductSelectedOption> findAllByProductId(UUID productId) {
    QProductSelectedOption productSelectedOption = QProductSelectedOption.productSelectedOption;

    return queryFactory
        .selectFrom(productSelectedOption)
        .where(productSelectedOption.productId.eq(productId))
        .orderBy(
            productSelectedOption.sortOrder.asc(),
            productSelectedOption.productOptionName.asc(),
            productSelectedOption.productOptionItemName.asc())
        .fetch();
  }
}
