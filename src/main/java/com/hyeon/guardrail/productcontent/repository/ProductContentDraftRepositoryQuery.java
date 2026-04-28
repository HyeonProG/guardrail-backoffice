package com.hyeon.guardrail.productcontent.repository;

import com.hyeon.guardrail.productcontent.domain.ProductContentDraft;
import com.hyeon.guardrail.productcontent.domain.ProductContentStatus;
import com.hyeon.guardrail.productcontent.domain.QProductContentDraft;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** 상품 설명 초안 Querydsl 조회 저장소 */
@Repository
public class ProductContentDraftRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductContentDraftRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 상품 설명 초안 단건 조회 */
  public Optional<ProductContentDraft> findById(UUID productId, UUID draftId) {
    QProductContentDraft draft = QProductContentDraft.productContentDraft;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(draft)
            .where(draft.id.eq(draftId), draft.productId.eq(productId), draft.deleted.isFalse())
            .fetchOne());
  }

  /** 삭제되지 않은 상품 설명 초안 목록 조회 */
  public List<ProductContentDraft> findAllByProductId(UUID productId, ProductContentStatus status) {
    QProductContentDraft draft = QProductContentDraft.productContentDraft;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(draft.productId.eq(productId));
    condition.and(draft.deleted.isFalse());

    if (status != null) {
      condition.and(draft.status.eq(status));
    }

    return queryFactory.selectFrom(draft).where(condition).orderBy(draft.createdAt.desc()).fetch();
  }

  /** 상품 기준 삭제되지 않은 초안 존재 여부 조회 */
  public boolean existsByProductId(UUID productId) {
    QProductContentDraft draft = QProductContentDraft.productContentDraft;

    Integer one =
        queryFactory
            .selectOne()
            .from(draft)
            .where(draft.productId.eq(productId), draft.deleted.isFalse())
            .fetchFirst();

    return one != null;
  }
}
