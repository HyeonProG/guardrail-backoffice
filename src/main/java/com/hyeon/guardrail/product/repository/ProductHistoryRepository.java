package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.product.domain.ProductHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 처리 이력 기본 영속성 저장소 */
public interface ProductHistoryRepository extends JpaRepository<ProductHistory, UUID> {}
