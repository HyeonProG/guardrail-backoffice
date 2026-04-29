package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.product.domain.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 기본 영속성 저장소 */
public interface ProductRepository extends JpaRepository<Product, UUID> {}
