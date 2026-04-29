package com.hyeon.guardrail.productoption.repository;

import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 옵션값 기본 영속성 저장소 */
public interface ProductOptionItemRepository extends JpaRepository<ProductOptionItem, UUID> {}
