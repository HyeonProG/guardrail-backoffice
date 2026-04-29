package com.hyeon.guardrail.productoption.repository;

import com.hyeon.guardrail.productoption.domain.ProductOption;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 옵션 그룹 기본 영속성 저장소 */
public interface ProductOptionRepository extends JpaRepository<ProductOption, UUID> {}
