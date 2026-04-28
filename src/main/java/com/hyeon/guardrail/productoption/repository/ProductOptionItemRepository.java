package com.hyeon.guardrail.productoption.repository;

import com.hyeon.guardrail.productoption.domain.ProductOptionItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 상품 옵션값 기본 영속성 저장소 */
public interface ProductOptionItemRepository extends JpaRepository<ProductOptionItem, UUID> {

  /** 옵션값 기본 정보 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update ProductOptionItem productOptionItem
         set productOptionItem.name = :name,
             productOptionItem.additionalPrice = :additionalPrice,
             productOptionItem.sortOrder = :sortOrder
       where productOptionItem.id = :productOptionItemId
         and productOptionItem.productOptionId = :productOptionId
         and productOptionItem.deleted = false
      """)
  int updateBasicInfo(
      @Param("productOptionId") UUID productOptionId,
      @Param("productOptionItemId") UUID productOptionItemId,
      @Param("name") String name,
      @Param("additionalPrice") int additionalPrice,
      @Param("sortOrder") int sortOrder);
}
