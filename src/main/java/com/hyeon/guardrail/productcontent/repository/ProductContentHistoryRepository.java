package com.hyeon.guardrail.productcontent.repository;

import com.hyeon.guardrail.productcontent.domain.ProductContentHistory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 설명 처리 이력 기본 영속성 저장소 */
public interface ProductContentHistoryRepository
    extends JpaRepository<ProductContentHistory, UUID> {}
