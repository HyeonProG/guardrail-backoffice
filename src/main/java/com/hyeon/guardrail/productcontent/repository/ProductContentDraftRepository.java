package com.hyeon.guardrail.productcontent.repository;

import com.hyeon.guardrail.productcontent.domain.ProductContentDraft;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 상품 설명 초안 기본 영속성 저장소 */
public interface ProductContentDraftRepository extends JpaRepository<ProductContentDraft, UUID> {

  /** 상품 설명 초안 본문 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update ProductContentDraft draft
         set draft.content = :content
       where draft.id = :draftId
         and draft.productId = :productId
         and draft.deleted = false
      """)
  int updateContent(
      @Param("draftId") UUID draftId,
      @Param("productId") UUID productId,
      @Param("content") String content);
}
