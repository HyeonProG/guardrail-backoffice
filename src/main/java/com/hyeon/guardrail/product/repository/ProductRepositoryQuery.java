package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.common.exception.BaseException;
import com.hyeon.guardrail.common.response.BaseResponseStatus;
import com.hyeon.guardrail.product.domain.Product;
import com.hyeon.guardrail.product.domain.ProductHistoryType;
import com.hyeon.guardrail.product.domain.ProductStatus;
import com.hyeon.guardrail.product.domain.QProduct;
import com.hyeon.guardrail.product.domain.QProductHistory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.JPAExpressions;
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

/** 상품 Querydsl 조회 저장소 */
@Repository
public class ProductRepositoryQuery {

  private final JPAQueryFactory queryFactory;

  public ProductRepositoryQuery(EntityManager entityManager) {
    this.queryFactory = new JPAQueryFactory(entityManager);
  }

  /** 삭제되지 않은 상품 단건 조회 */
  public Optional<Product> findById(UUID productId) {
    QProduct product = QProduct.product;

    return Optional.ofNullable(
        queryFactory
            .selectFrom(product)
            .where(product.id.eq(productId), product.deleted.isFalse())
            .fetchOne());
  }

  /** 삭제되지 않은 상품 목록 조회 */
  public Page<Product> findAll(
      UUID categoryId, ProductStatus status, UUID ownerId, Pageable pageable) {
    QProduct product = QProduct.product;
    QProductHistory productHistory = QProductHistory.productHistory;
    BooleanBuilder condition = new BooleanBuilder();
    condition.and(product.deleted.isFalse());

    if (categoryId != null) {
      condition.and(product.categoryId.eq(categoryId));
    }

    if (status != null) {
      condition.and(product.status.eq(status));
    }

    if (ownerId != null) {
      condition.and(
          JPAExpressions.selectOne()
              .from(productHistory)
              .where(
                  productHistory.productId.eq(product.id),
                  productHistory.actorId.eq(ownerId),
                  productHistory.type.eq(ProductHistoryType.CREATED))
              .exists());
    }

    List<Product> content =
        queryFactory
            .selectFrom(product)
            .where(condition)
            .orderBy(toOrderSpecifiers(pageable.getSort()))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long total = queryFactory.select(product.count()).from(product).where(condition).fetchOne();

    return new PageImpl<>(content, pageable, total == null ? 0L : total);
  }

  private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
    QProduct product = QProduct.product;

    if (sort.isUnsorted()) {
      return new OrderSpecifier<?>[] {product.createdAt.desc()};
    }

    return sort.stream()
        .map(order -> toOrderSpecifier(product, order))
        .toArray(OrderSpecifier[]::new);
  }

  private OrderSpecifier<?> toOrderSpecifier(QProduct product, Sort.Order sortOrder) {
    Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;

    return switch (sortOrder.getProperty()) {
      case "createdAt" -> new OrderSpecifier<>(direction, product.createdAt);
      case "updatedAt" -> new OrderSpecifier<>(direction, product.updatedAt);
      case "name" -> new OrderSpecifier<>(direction, product.name);
      case "quantity" -> new OrderSpecifier<>(direction, product.quantity);
      case "status" -> new OrderSpecifier<>(direction, product.status);
      default -> throw new BaseException(BaseResponseStatus.INVALID_REQUEST, "허용되지 않은 정렬 필드입니다.");
    };
  }
}
