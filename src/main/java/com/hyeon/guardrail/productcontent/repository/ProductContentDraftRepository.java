package com.hyeon.guardrail.productcontent.repository;

import com.hyeon.guardrail.productcontent.domain.ProductContentDraft;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** 상품 설명 초안 기본 영속성 저장소 */
public interface ProductContentDraftRepository extends JpaRepository<ProductContentDraft, UUID> {

  /** 상품별 설명 초안 전체 삭제 */
  void deleteByProductId(UUID productId);
}
