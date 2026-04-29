package com.hyeon.guardrail.productoption.repository;

import com.hyeon.guardrail.productoption.domain.ProductOption;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 상품 옵션 그룹 기본 영속성 저장소 */
public interface ProductOptionRepository extends JpaRepository<ProductOption, UUID> {

  /** 옵션 그룹 기본 정보 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update ProductOption productOption
         set productOption.name = :name,
             productOption.sortOrder = :sortOrder
       where productOption.id = :productOptionId
         and productOption.categoryId = :categoryId
         and productOption.deleted = false
      """)
  int updateBasicInfo(
      @Param("categoryId") UUID categoryId,
      @Param("productOptionId") UUID productOptionId,
      @Param("name") String name,
      @Param("sortOrder") int sortOrder);
}
