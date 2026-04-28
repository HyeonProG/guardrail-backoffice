package com.hyeon.guardrail.product.repository;

import com.hyeon.guardrail.product.domain.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 상품 기본 영속성 저장소 */
public interface ProductRepository extends JpaRepository<Product, UUID> {

  /** 상품 기본 정보 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update Product product
         set product.categoryId = :categoryId,
             product.name = :name,
             product.description = :description,
             product.quantity = :quantity
       where product.id = :productId
         and product.deleted = false
      """)
  int updateBasicInfo(
      @Param("productId") UUID productId,
      @Param("categoryId") UUID categoryId,
      @Param("name") String name,
      @Param("description") String description,
      @Param("quantity") int quantity);
}
