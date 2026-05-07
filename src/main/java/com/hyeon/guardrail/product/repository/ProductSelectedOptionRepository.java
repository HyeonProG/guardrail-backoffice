package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.product.domain.ProductSelectedOption;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 선택 옵션 영속성 저장소 */
public interface ProductSelectedOptionRepository
    extends JpaRepository<ProductSelectedOption, UUID> {

  /** 상품 기준 선택 옵션 스냅샷 삭제 */
  void deleteByProductId(UUID productId);
}
