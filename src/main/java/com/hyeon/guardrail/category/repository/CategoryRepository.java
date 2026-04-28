package com.hyeon.guardrail.category.repository;

import com.hyeon.guardrail.category.domain.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 카테고리 기본 영속성 저장소 */
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  /** 삭제되지 않은 최상위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdIsNullAndNameAndDeletedFalse(String name);

  /** 삭제되지 않은 하위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdAndNameAndDeletedFalse(UUID parentId, String name);

  /** 특정 카테고리를 제외한 삭제되지 않은 최상위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdIsNullAndNameAndDeletedFalseAndIdNot(String name, UUID categoryId);

  /** 특정 카테고리를 제외한 삭제되지 않은 하위 카테고리명 존재 여부 조회 */
  boolean existsByParentIdAndNameAndDeletedFalseAndIdNot(
      UUID parentId, String name, UUID categoryId);

  /** 카테고리 기본 정보 수정 */
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      """
      update Category category
         set category.parentId = :parentId,
             category.name = :name
       where category.id = :categoryId
         and category.deleted = false
      """)
  int updateBasicInfo(
      @Param("categoryId") UUID categoryId,
      @Param("parentId") UUID parentId,
      @Param("name") String name);
}
